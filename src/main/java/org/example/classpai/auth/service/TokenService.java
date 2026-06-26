package org.example.classpai.auth.service;

import com.alibaba.fastjson2.JSON;
import org.example.classpai.entity.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Token 服务 — 将 Token 存入 Redis，24 小时过期
 */
@Service
public class TokenService {

    private static final String TOKEN_PREFIX = "auth:token:";
    private static final long TOKEN_TTL_HOURS = 1;

    private final StringRedisTemplate redisTemplate;

    public TokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成 Token 并存入 Redis
     */
    public String generateToken(User user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key,
                JSON.toJSONString(user),
                TOKEN_TTL_HOURS, TimeUnit.HOURS);
        return token;
    }

    /**
     * 校验 Token，返回用户信息；无效则返回 null
     */
    public User validateToken(String token) {
        if (token == null || token.isBlank()) return null;
        String json = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (json == null) return null;
        return JSON.parseObject(json, User.class);
    }

    /**
     * 刷新 Token 有效期
     */
    public void refreshToken(String token) {
        String key = TOKEN_PREFIX + token;
        redisTemplate.expire(key, TOKEN_TTL_HOURS, TimeUnit.HOURS);
    }

    /**
     * 销毁 Token（登出时调用）
     */
    public void revokeToken(String token) {
        redisTemplate.delete(TOKEN_PREFIX + token);
    }
}
