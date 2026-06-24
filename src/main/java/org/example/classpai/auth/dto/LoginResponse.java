package org.example.classpai.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录成功响应 / 用户信息响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String userName;
    private String role;
    private String phone;
    private LocalDateTime createTime;
    private String school;
    private String college;
    private String major;
}
