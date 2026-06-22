package org.example.classpai.service;

import org.example.classpai.common.Result;
import org.example.classpai.dto.LoginDTO;
import org.example.classpai.dto.RegisterDTO;
import org.example.classpai.entity.User;

public interface UserService {

    Result<?> register(RegisterDTO dto);

    Result<User> login(LoginDTO dto, jakarta.servlet.http.HttpSession session);

    Result<User> getCurrentUser(jakarta.servlet.http.HttpSession session);
}
