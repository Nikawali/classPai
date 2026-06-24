package org.example.classpai.auth.service;

import org.example.classpai.auth.dto.LoginRequest;
import org.example.classpai.auth.dto.LoginResponse;
import org.example.classpai.auth.dto.UserProfileResponse;

public interface AuthService {

    /** 用户登录 */
    LoginResponse login(LoginRequest request, String clientIp);

    /** 根据 Token 获取用户详情 */
    UserProfileResponse getProfile(String token);
}
