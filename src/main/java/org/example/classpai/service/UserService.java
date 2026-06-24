package org.example.classpai.service;

import org.example.classpai.common.Result;
import org.example.classpai.dto.RegisterDTO;

public interface UserService {

    /** 发送验证码 */
    Result<?> sendCode(String phone);

    /** 注册 */
    Result<?> register(RegisterDTO dto);
}
