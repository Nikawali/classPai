package org.example.classpai.auth.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * - 每个 IP 每分钟最多 10 次登录请求
 * - 同一账号连续 5 次失败后锁定 15 分钟
 */
@Component
public class LoginRateLimiter {

    private static final int IP_MAX_REQUESTS = 10;
    private static final int IP_WINDOW_SECONDS = 60;
    private static final int MAX_FAILURES = 5;
    private static final int LOCK_DURATION_SECONDS = 15 * 60;
    private static final int FAIL_TTL_SECONDS = 30 * 60; // 失败计数 30 分钟自动清理

    private static final String IP_KEY_PREFIX = "auth:ratelimit:ip:";
    private static final String LOCK_KEY_PREFIX = "auth:lockout:locked:";
    private static final String FAIL_KEY_PREFIX = "auth:lockout:fail:";

    private final StringRedisTemplate redisTemplate;

    public LoginRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * @return true=允许，false=被限流
     */
    public boolean tryAcquireIP(String ip) {
        String key = IP_KEY_PREFIX + ip;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, IP_WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        return count == null || count <= IP_MAX_REQUESTS;
    }

    /**
     * 记录一次登录失败
     * @return true=已被锁定
     */
    public boolean recordFailure(String userAccount) {
        // 已在锁定中
        if (Boolean.TRUE.equals(redisTemplate.hasKey(LOCK_KEY_PREFIX + userAccount))) {
            return true;
        }

        String failKey = FAIL_KEY_PREFIX + userAccount;
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            redisTemplate.expire(failKey, FAIL_TTL_SECONDS, TimeUnit.SECONDS);
        }

        if (count != null && count >= MAX_FAILURES) {
            redisTemplate.opsForValue().set(
                    LOCK_KEY_PREFIX + userAccount,
                    "1",
                    LOCK_DURATION_SECONDS, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    /**
     * 检查账号是否被锁定
     */
    public boolean isLocked(String userAccount) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(LOCK_KEY_PREFIX + userAccount));
    }

    /**
     * 登录成功后清除失败记录
     */
    public void clear(String userAccount) {
        redisTemplate.delete(FAIL_KEY_PREFIX + userAccount);
        redisTemplate.delete(LOCK_KEY_PREFIX + userAccount);
    }

    /**
     * 获取剩余锁定秒数
     */
    public long lockLeftSeconds(String userAccount) {
        Long ttl = redisTemplate.getExpire(LOCK_KEY_PREFIX + userAccount, TimeUnit.SECONDS);
        return ttl == null || ttl < 0 ? 0 : ttl;
    }
}
