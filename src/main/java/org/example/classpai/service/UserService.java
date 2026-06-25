package org.example.classpai.service;

import org.example.classpai.common.Result;
import org.example.classpai.dto.RegisterDTO;
import org.example.classpai.dto.UpdateProfileDTO;

public interface UserService {

    /** 发送验证码（注册） */
    Result<?> sendCode(String phone);

    /** 注册 */
    Result<?> register(RegisterDTO dto);

    /** 发送验证码（修改个人信息） */
    Result<?> sendCodeForProfile(String phone, Long currentUserId);

    /** 修改个人信息（手机号/密码/学院/专业） */
    Result<?> updateProfile(UpdateProfileDTO dto, Long currentUserId);
}
