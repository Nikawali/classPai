package org.example.classpai.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.classpai.auth.dto.LoginRequest;
import org.example.classpai.auth.dto.LoginResponse;
import org.example.classpai.auth.dto.UserProfileResponse;
import org.example.classpai.auth.service.AuthService;
import org.example.classpai.auth.service.TokenService;
import org.example.classpai.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录控制器 — POST /auth/login
 * 路由前缀 /auth 在 WebConfig 拦截器排除范围之外，完全独立于原有 /api 体系
 */
@RestController
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/auth/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        LoginResponse result = authService.login(request, clientIp);
        return Result.success(result);
    }

    /**
     * 获取当前用户信息 — GET /auth/me
     * Header: Authorization: Bearer <token>
     */
    @GetMapping("/auth/me")
    public Result<UserProfileResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        UserProfileResponse profile = authService.getProfile(token);
        return Result.success(profile);
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new org.example.classpai.common.exception.BusinessException(401, "未登录或Token无效");
    }

    /**
     * 获取客户端真实 IP（考虑代理）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
