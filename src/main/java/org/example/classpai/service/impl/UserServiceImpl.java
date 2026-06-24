package org.example.classpai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.classpai.common.Result;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.dto.RegisterDTO;
import org.example.classpai.dto.UpdateProfileDTO;
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

    @Override
    public Result<?> sendCodeForProfile(String phone, Long currentUserId) {
        // 手机号格式校验（与注册模块一致）
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new BusinessException(400, "请输入正确的11位手机号");
        }

        // 检查该手机号是否已被其他用户绑定
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone)
                .ne(User::getUserId, currentUserId);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "该手机号已被其他用户绑定");
        }

        // 如果已有未过期的验证码，直接复用（与注册逻辑一致）
        String redisKey = "sms:profile:" + phone;
        String code = redisTemplate.opsForValue().get(redisKey);
        if (code != null) {
            return Result.success(code);
        }

        // 生成6位随机验证码，有效期5分钟
        code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);

        System.out.println("【个人信息修改】验证码发送到 " + phone + ": " + code);
        return Result.success(code);
    }

    @Override
    @Transactional
    public Result<?> updateProfile(UpdateProfileDTO dto, Long currentUserId) {
        // ---------- 1. 查询当前用户 ----------
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // ---------- 2. 短信验证码校验（修改手机号时必填） ----------
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            if (dto.getSmsCode() == null || dto.getSmsCode().isBlank()) {
                throw new BusinessException(400, "修改手机号需提供短信验证码");
            }
            String savedCode = redisTemplate.opsForValue().get("sms:profile:" + dto.getPhone());
            if (savedCode == null || !savedCode.equals(dto.getSmsCode())) {
                throw new BusinessException(400, "验证码错误或已过期");
            }
        }

        // ---------- 3. 手机号修改校验 ----------
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            // 格式校验
            if (!dto.getPhone().matches("^1\\d{10}$")) {
                throw new BusinessException(400, "手机号格式不正确");
            }
            // 唯一性校验（排除当前用户）
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, dto.getPhone())
                    .ne(User::getUserId, currentUserId);
            if (userMapper.selectCount(phoneWrapper) > 0) {
                throw new BusinessException(400, "该手机号已被其他用户绑定");
            }
            user.setPhone(dto.getPhone());
            // 清除已使用的验证码
            redisTemplate.delete("sms:profile:" + dto.getPhone());
        }

        // ---------- 4. 密码修改校验 ----------
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            if (dto.getPassword().length() < 6) {
                throw new BusinessException(400, "密码长度至少6位");
            }
            user.setPassword(PasswordUtils.encode(dto.getPassword()));
        }

        // ---------- 5. 学院修改校验 ----------
        if (dto.getCollege() != null && !dto.getCollege().isBlank()) {
            String college = dto.getCollege().trim();
            if (college.length() > 50) {
                throw new BusinessException(400, "学院名称不能超过50字符");
            }
            user.setCollege(college);
        }

        // ---------- 6. 专业修改校验 ----------
        if (dto.getMajor() != null && !dto.getMajor().isBlank()) {
            String major = dto.getMajor().trim();
            if (major.length() > 50) {
                throw new BusinessException(400, "专业名称不能超过50字符");
            }
            user.setMajor(major);
        }

        // ---------- 7. 保存修改 ----------
        userMapper.updateById(user);

        return Result.success("个人信息修改成功");
    }
}
