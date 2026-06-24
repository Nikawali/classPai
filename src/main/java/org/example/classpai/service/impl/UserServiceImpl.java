package org.example.classpai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.classpai.common.Result;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.dto.RegisterDTO;
import org.example.classpai.entity.User;
import org.example.classpai.mapper.UserMapper;
import org.example.classpai.service.UserService;
import org.example.classpai.utils.PasswordUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    public UserServiceImpl(UserMapper userMapper, StringRedisTemplate redisTemplate) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Result<?> sendCode(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "手机号已被注册");
        }

        String code = redisTemplate.opsForValue().get("sms:register:" + phone);
        if (code != null) {
            return Result.success(code);
        }

        code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set("sms:register:" + phone, code, 5, TimeUnit.MINUTES);

        System.out.println("验证码发送到 " + phone + ": " + code);
        return Result.success(code);
    }

    @Override
    @Transactional
    public Result<?> register(RegisterDTO dto) {
        String savedCode = redisTemplate.opsForValue().get("sms:register:" + dto.getPhone());
        if (savedCode == null || !savedCode.equals(dto.getCode())) {
            throw new BusinessException(400, "验证码错误或已过期");
        }

        if (userMapper.selectById(dto.getUserId()) != null) {
            throw new BusinessException(400, "学号/工号已存在");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "手机号已被注册");
        }

        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(PasswordUtils.encode(dto.getPassword()));
        userMapper.insert(user);

        redisTemplate.delete("sms:register:" + dto.getPhone());

        return Result.success("注册成功");
    }
}
