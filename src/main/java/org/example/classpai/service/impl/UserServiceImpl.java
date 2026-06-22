package org.example.classpai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.classpai.common.Result;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.dto.LoginDTO;
import org.example.classpai.dto.RegisterDTO;
import org.example.classpai.entity.User;
import org.example.classpai.mapper.UserMapper;
import org.example.classpai.service.UserService;
import org.example.classpai.utils.PasswordUtils;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Result<?> register(RegisterDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtils.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        userMapper.insert(user);
        return Result.success("注册成功");
    }

    @Override
    public Result<User> login(LoginDTO dto, jakarta.servlet.http.HttpSession session) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null || !PasswordUtils.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        // 脱敏
        user.setPassword(null);
        session.setAttribute("user", user);
        return Result.success(user);
    }

    @Override
    public Result<User> getCurrentUser(jakarta.servlet.http.HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.success(user);
    }
}
