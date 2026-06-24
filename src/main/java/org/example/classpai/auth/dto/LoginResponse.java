package org.example.classpai.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录成功响应
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String userName;
    private String role;
}
