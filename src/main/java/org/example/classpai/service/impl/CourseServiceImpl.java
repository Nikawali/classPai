package org.example.classpai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.classpai.common.PageResult;
import org.example.classpai.common.Result;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.dto.CourseDTO;
import org.example.classpai.entity.*;
import org.example.classpai.mapper.*;
import org.example.classpai.service.CourseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final UserCourseMapper userCourseMapper;

    public CourseServiceImpl(CourseMapper courseMapper,
                             UserCourseMapper userCourseMapper) {
        this.courseMapper = courseMapper;
        this.userCourseMapper = userCourseMapper;
    }

    @Override
    @Transactional
    public Result<Course> createCourse(CourseDTO dto, User user) {
        Course course = new Course();
        BeanUtils.copyProperties(dto, course);
        course.setCourseCode(generateCourseCode());
        courseMapper.insert(course);

        // 创建者默认为该课程教师
        UserCourse uc = new UserCourse();
        uc.setUserId(user.getUserId());
        uc.setCourseId(course.getCourseId());
        uc.setRole("teacher");
        userCourseMapper.insert(uc);

        return Result.success(course);
    }

    @Override
    public PageResult<Course> listMyCourses(User user, int page, int pageSize) {
        LambdaQueryWrapper<UserCourse> ucWrapper = new LambdaQueryWrapper<>();
        ucWrapper.eq(UserCourse::getUserId, user.getUserId())
                 .eq(UserCourse::getRole, "teacher");
        List<Long> courseIds = userCourseMapper.selectList(ucWrapper)
                .stream().map(UserCourse::getCourseId).collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return PageResult.of(0, List.of(), page, pageSize);
        }

        Page<Course> p = courseMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Course>().in(Course::getCourseId, courseIds)
                        .orderByDesc(Course::getCreateTime));
        return PageResult.of(p.getTotal(), p.getRecords(), p.getCurrent(), p.getSize());
    }

    @Override
    @Transactional
    public Result<?> joinCourse(String courseCode, User user) {
        LambdaQueryWrapper<Course> cw = new LambdaQueryWrapper<>();
        cw.eq(Course::getCourseCode, courseCode);
        Course course = courseMapper.selectOne(cw);
        if (course == null) {
            throw new BusinessException(400, "选课码无效");
        }

        // 检查是否已加入
        LambdaQueryWrapper<UserCourse> ucWrapper = new LambdaQueryWrapper<>();
        ucWrapper.eq(UserCourse::getUserId, user.getUserId())
                 .eq(UserCourse::getCourseId, course.getCourseId());
        if (userCourseMapper.selectCount(ucWrapper) > 0) {
            throw new BusinessException(400, "已加入该课程");
        }

        // 加入者默认为学生
        UserCourse uc = new UserCourse();
        uc.setUserId(user.getUserId());
        uc.setCourseId(course.getCourseId());
        uc.setRole("student");
        userCourseMapper.insert(uc);

        return Result.success("加入成功");
    }

    @Override
    public PageResult<Course> listJoinedCourses(User user, int page, int pageSize) {
        LambdaQueryWrapper<UserCourse> ucWrapper = new LambdaQueryWrapper<>();
        ucWrapper.eq(UserCourse::getUserId, user.getUserId())
                 .eq(UserCourse::getRole, "student");
        List<Long> courseIds = userCourseMapper.selectList(ucWrapper)
                .stream().map(UserCourse::getCourseId).collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return PageResult.of(0, List.of(), page, pageSize);
        }

        Page<Course> p = courseMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Course>().in(Course::getCourseId, courseIds)
                        .orderByDesc(Course::getCreateTime));
        return PageResult.of(p.getTotal(), p.getRecords(), p.getCurrent(), p.getSize());
    }

    @Override
    public Result<Course> getCourseDetail(Long courseId, User user) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        if (!isCourseMember(courseId, user.getUserId())) {
            throw new BusinessException(403, "非课程成员，无权查看");
        }

        return Result.success(course);
    }

    /** 判断用户是否是该课程成员（无论角色） */
    private boolean isCourseMember(Long courseId, Long userId) {
        return userCourseMapper.selectCount(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getCourseId, courseId)
                        .eq(UserCourse::getUserId, userId)) > 0;
    }

    /** 生成6位不重复选课码 */
    private String generateCourseCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(r.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (courseMapper.selectCount(
                new LambdaQueryWrapper<Course>().eq(Course::getCourseCode, code)) > 0);
        return code;
    }
}
