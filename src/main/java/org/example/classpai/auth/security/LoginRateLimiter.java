package org.example.classpai.auth.security;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录限流与防暴力破解
 * 
 * - 每个 IP 每分钟最多 10 次登录请求
 * - 同一账号连续 5 次失败后锁定 15 分钟
 */
@Component
public class LoginRateLimiter {

    // ========== IP 限流 ==========
    private static final int IP_MAX_REQUESTS = 10;
    private static final long IP_WINDOW_MS = 60_000;

    private static class IpBucket {
        long windowStart = System.currentTimeMillis();
        int count;
    }
    private final ConcurrentHashMap<String, IpBucket> ipBuckets = new ConcurrentHashMap<>();

    /**
     * @return true=允许，false=被限流
     */
    public boolean tryAcquireIP(String ip) {
        ipBuckets.entrySet().removeIf(e ->
                System.currentTimeMillis() - e.getValue().windowStart > IP_WINDOW_MS);

        IpBucket bucket = ipBuckets.computeIfAbsent(ip, k -> new IpBucket());
        synchronized (bucket) {
            if (System.currentTimeMillis() - bucket.windowStart > IP_WINDOW_MS) {
                bucket.windowStart = System.currentTimeMillis();
                bucket.count = 0;
            }
            if (bucket.count >= IP_MAX_REQUESTS) {
                return false;
            }
            bucket.count++;
            return true;
        }
    }

    // ========== 账号锁定 ==========
    private static final int MAX_FAILURES = 5;
    private static final long LOCK_DURATION_MS = 15 * 60_000;

    private final ConcurrentHashMap<String, Integer> failureCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lockedUntil = new ConcurrentHashMap<>();

    /**
     * 记录一次登录失败
     * @return true=已被锁定
     */
    public boolean recordFailure(String username) {
        Long lockEnd = lockedUntil.get(username);
        if (lockEnd != null && System.currentTimeMillis() < lockEnd) {
            return true; // 仍在锁定
        }
        lockedUntil.remove(username);
        int count = failureCount.merge(username, 1, Integer::sum);
        if (count >= MAX_FAILURES) {
            lockedUntil.put(username, System.currentTimeMillis() + LOCK_DURATION_MS);
            return true;
        }
        return false;
    }

    /**
     * 检查账号是否被锁定
     */
    public boolean isLocked(String username) {
        Long lockEnd = lockedUntil.get(username);
        if (lockEnd != null && System.currentTimeMillis() < lockEnd) {
            return true;
        }
        if (lockEnd != null) {
            lockedUntil.remove(username);
            failureCount.remove(username);
        }
        return false;
    }

    /**
     * 登录成功后清除失败记录
     */
    public void clear(String username) {
        failureCount.remove(username);
        lockedUntil.remove(username);
    }

    /**
     * 获取剩余锁定秒数
     */
    public long lockLeftSeconds(String username) {
        Long lockEnd = lockedUntil.get(username);
        if (lockEnd == null) return 0;
        long left = (lockEnd - System.currentTimeMillis()) / 1000;
        return Math.max(0, left);
    }
}
