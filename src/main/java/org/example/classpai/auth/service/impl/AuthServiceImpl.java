package org.example.classpai.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.classpai.auth.dto.LoginRequest;
import org.example.classpai.auth.dto.LoginResponse;
import org.example.classpai.auth.security.LoginRateLimiter;
import org.example.classpai.auth.service.AuthService;
import org.example.classpai.auth.service.TokenService;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.entity.User;
import org.example.classpai.mapper.UserMapper;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final LoginRateLimiter rateLimiter;

    public AuthServiceImpl(UserMapper userMapper,
                           TokenService tokenService,
                           LoginRateLimiter rateLimiter) {
        this.userMapper = userMapper;
        this.tokenService = tokenService;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        String account = request.getUsername().trim();

        // ---------- 1. IP 限流 ----------
        if (!rateLimiter.tryAcquireIP(clientIp)) {
            log.warn("IP {} 请求过于频繁，已被限流", clientIp);
            throw new BusinessException(429, "请求过于频繁，请稍后再试");
        }

        // ---------- 2. 账号锁定检查 ----------
        if (rateLimiter.isLocked(account)) {
            long left = rateLimiter.lockLeftSeconds(account);
            log.warn("账号 {} 已被锁定，剩余 {} 秒", account, left);
            throw new BusinessException(423, "账号已被临时锁定，请 " + left + " 秒后重试");
        }

        // ---------- 3. 查询用户：手机号 或 userId ----------
        User user = queryByPhoneOrId(account);

        if (user == null) {
            rateLimiter.recordFailure(account);
            throw new BusinessException(400, "账号或密码错误");
        }

        // ---------- 4. 密码校验（BCrypt 加盐哈希比对） ----------
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            rateLimiter.recordFailure(account);
            throw new BusinessException(400, "账号或密码错误");
        }

        // ---------- 5. 清除失败记录，生成 Token ----------
        rateLimiter.clear(account);

        LoginResponse userInfo = new LoginResponse();
        userInfo.setUserId(user.getUserId());
        userInfo.setUserName(user.getUserName());
        userInfo.setRole(user.getRole());

        String token = tokenService.generateToken(userInfo);
        userInfo.setToken(token);

        log.info("用户 {} (userId={}) 登录成功", user.getUserName(), user.getUserId());
        return userInfo;
    }

    @Override
    public LoginResponse getProfile(String token) {
        LoginResponse tokenInfo = tokenService.validateToken(token);
        if (tokenInfo == null) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        User user = userMapper.selectById(tokenInfo.getUserId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        LoginResponse profile = new LoginResponse();
        profile.setUserId(user.getUserId());
        profile.setUserName(user.getUserName());
        profile.setRole(user.getRole());
        profile.setPhone(user.getPhone());
        profile.setCreateTime(user.getCreateTime());
        profile.setSchool(user.getSchool());
        profile.setCollege(user.getCollege());
        profile.setMajor(user.getMajor());
        return profile;
    }

    /** 按手机号或 userId 查询用户 */
    private User queryByPhoneOrId(String account) {
        // 优先按手机号查
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, account));
        if (user != null) return user;

        // 尝试按 userId 查
        try {
            Long uid = Long.parseLong(account);
            return userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUserId, uid));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
