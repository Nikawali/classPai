package org.example.classpai.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.classpai.auth.dto.LoginRequest;
import org.example.classpai.auth.dto.LoginResponse;
import org.example.classpai.auth.service.AuthService;
import org.example.classpai.common.Result;
import org.example.classpai.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录控制器 — POST /auth/login
 * /auth/me 由 LoginInterceptor 统一校验，直接读取 currentUser
 */
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        LoginResponse result = authService.login(request, clientIp);
        return Result.success(result);
    }

    /**
     * 获取当前登录用户信息 — GET /auth/me
     * Token 校验已由 LoginInterceptor 完成，直接读取 currentUser
     */
    @GetMapping("/auth/me")
    public Result<LoginResponse> getProfile(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        LoginResponse resp = new LoginResponse();
        resp.setUserId(user.getUserId());
        resp.setUserName(user.getUserName());
        resp.setRole(user.getRole());
        resp.setPhone(user.getPhone());
        resp.setCreateTime(user.getCreateTime());
        resp.setSchool(user.getSchool());
        resp.setCollege(user.getCollege());
        resp.setMajor(user.getMajor());
        return Result.success(resp);
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
