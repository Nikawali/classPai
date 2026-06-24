package org.example.classpai.auth.dto;

import lombok.Data;

/**
 * 用户详情（我的页面）
 */
@Data
public class UserProfileResponse {
    private Long userId;
    private String userName;
    private String phone;
    private String role;
    private String createTime;
}
