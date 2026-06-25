package org.example.classpai.dto;

import lombok.Data;

/**
 * 用户信息修改请求 DTO
 * 仅开放手机号、密码、专业、学院四项的修改权限
 */
@Data
public class UpdateProfileDTO {
    /** 新手机号（可选，修改时需同时提供 smsCode） */
    private String phone;

    /** 新密码（可选） */
    private String password;

    /** 学院（可选） */
    private String college;

    /** 专业（可选） */
    private String major;

    /** 短信验证码（修改手机号时必填） */
    private String smsCode;
}
