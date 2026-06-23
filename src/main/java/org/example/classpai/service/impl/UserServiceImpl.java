package org.example.classpai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.classpai.common.Result;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.dto.LoginDTO;
import org.example.classpai.dto.RegisterDTO;
import org.example.classpai.entity.Student;
import org.example.classpai.entity.Teacher;
import org.example.classpai.entity.User;
import org.example.classpai.mapper.StudentMapper;
import org.example.classpai.mapper.TeacherMapper;
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
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final StringRedisTemplate redisTemplate;

    public UserServiceImpl(UserMapper userMapper, StudentMapper studentMapper,
                           TeacherMapper teacherMapper, StringRedisTemplate redisTemplate) {
        this.userMapper = userMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Result<?> sendCode(String phone) {
        // 校验手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "手机号已被注册");
        }

        // 1分钟内重复发送返回原验证码
        String code = redisTemplate.opsForValue().get("sms:register:" + phone);
        if (code != null) {
            return Result.success(code);
        }

        // 生成6位验证码，存入Redis，5分钟过期
        code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set("sms:register:" + phone, code, 5, TimeUnit.MINUTES);

        System.out.println("验证码发送到 " + phone + ": " + code);
        return Result.success(code);
    }

    @Override
    @Transactional
    public Result<?> register(RegisterDTO dto) {
        // 1. 校验验证码
        String savedCode = redisTemplate.opsForValue().get("sms:register:" + dto.getPhone());
        if (savedCode == null || !savedCode.equals(dto.getCode())) {
            throw new BusinessException(400, "验证码错误或已过期");
        }

        // 2. 校验 userId 唯一
        if (userMapper.selectById(dto.getUserId()) != null) {
            throw new BusinessException(400, "学号/工号已存在");
        }

        // 3. 校验手机号唯一
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "手机号已被注册");
        }

        // 4. 插入 user 表
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(PasswordUtils.encode(dto.getPassword()));
        userMapper.insert(user);

        // 5. 根据角色插入 student 或 teacher 表
        if ("student".equals(dto.getRole())) {
            Student student = new Student();
            BeanUtils.copyProperties(dto, student);
            student.setStudentId(dto.getUserId());
            student.setName(dto.getUserName());
            studentMapper.insert(student);
        } else {
            Teacher teacher = new Teacher();
            BeanUtils.copyProperties(dto, teacher);
            teacher.setTeacherId(dto.getUserId());
            teacher.setName(dto.getUserName());
            teacherMapper.insert(teacher);
        }

        // 6. 删除验证码
        redisTemplate.delete("sms:register:" + dto.getPhone());

        return Result.success("注册成功");
    }

    @Override
    public Result<User> login(LoginDTO dto, jakarta.servlet.http.HttpSession session) {
        // 先按 userId(Long) 查，再按 phone(String) 查
        User user = null;
        try {
            long uid = Long.parseLong(dto.getUserId());
            user = userMapper.selectById(uid);
        } catch (NumberFormatException e) {
            // account 不是纯数字，按手机号查
        }
        if (user == null) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, dto.getUserId());
            user = userMapper.selectOne(wrapper);
        }

        if (user == null || !PasswordUtils.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }

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
