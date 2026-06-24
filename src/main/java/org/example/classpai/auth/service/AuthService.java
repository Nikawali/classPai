package org.example.classpai.auth.service;

import org.example.classpai.auth.dto.LoginRequest;
import org.example.classpai.auth.dto.LoginResponse;

public interface AuthService {

    /**
     * 用户登录
     * @param request  用户名 + 密码
     * @param clientIp 客户端 IP（用于限流）
     * @return 登录结果（含 Token）
     */
    LoginResponse login(LoginRequest request, String clientIp);

    /**
     * 获取当前登录用户信息
     * @param token 认证 Token
     * @return 用户详细信息
     */
    LoginResponse getProfile(String token);
}
