package org.example.classpai.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.classpai.auth.dto.LoginResponse;
import org.example.classpai.auth.service.TokenService;
import org.example.classpai.entity.User;
import org.example.classpai.mapper.UserMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final UserMapper userMapper;

    public LoginInterceptor(TokenService tokenService, UserMapper userMapper) {
        this.tokenService = tokenService;
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            write401(response);
            return false;
        }
        String token = auth.substring(7);
        LoginResponse loginInfo = tokenService.validateToken(token);
        if (loginInfo == null) {
            write401(response);
            return false;
        }
        User user = userMapper.selectById(loginInfo.getUserId());
        if (user == null) {
            write401(response);
            return false;
        }
        user.setPassword(null);
        request.setAttribute("currentUser", user);
        request.setAttribute("token", token);
        tokenService.refreshToken(token);
        return true;
    }

    private void write401(HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
    }
}
