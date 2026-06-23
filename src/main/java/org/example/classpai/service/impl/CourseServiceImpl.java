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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final TeacherCourseMapper teacherCourseMapper;
    private final UserCourseMapper userCourseMapper;

    public CourseServiceImpl(CourseMapper courseMapper,
                             TeacherCourseMapper teacherCourseMapper,
                             UserCourseMapper userCourseMapper) {
        this.courseMapper = courseMapper;
        this.teacherCourseMapper = teacherCourseMapper;
        this.userCourseMapper = userCourseMapper;
    }

    @Override
    @Transactional
    public Result<Course> createCourse(CourseDTO dto, User user) {
        Course course = new Course();
        course.setCourseName(dto.getCourseName());
        course.setCourseIntro(dto.getCourseIntro());
        course.setStartDate(dto.getStartDate());
        course.setEndDate(dto.getEndDate());
        course.setCourseCode(generateCourseCode());
        courseMapper.insert(course);

        // 创建者自动绑定为教师
        TeacherCourse tc = new TeacherCourse();
        tc.setTeacherUserId(user.getUserId());
        tc.setCourseId(course.getCourseId());
        tc.setRoleInCourse(1); // 1=主讲教师
        teacherCourseMapper.insert(tc);

        return Result.success(course);
    }

    @Override
    public PageResult<Course> listMyCourses(User user, int page, int pageSize) {
        // 查 teacher_course 表中此用户教的课程
        LambdaQueryWrapper<TeacherCourse> tcWrapper = new LambdaQueryWrapper<>();
        tcWrapper.eq(TeacherCourse::getTeacherUserId, user.getUserId());
        List<Long> courseIds = teacherCourseMapper.selectList(tcWrapper)
                .stream().map(TeacherCourse::getCourseId).collect(Collectors.toList());

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
            throw new BusinessException(400, "课程码无效");
        }

        // 查重
        LambdaQueryWrapper<UserCourse> ucWrapper = new LambdaQueryWrapper<>();
        ucWrapper.eq(UserCourse::getStudentId, user.getUserId())
                 .eq(UserCourse::getCourseId, course.getCourseId());
        if (userCourseMapper.selectCount(ucWrapper) > 0) {
            throw new BusinessException(400, "已加入该课程");
        }

        UserCourse uc = new UserCourse();
        uc.setStudentId(user.getUserId());
        uc.setCourseId(course.getCourseId());
        userCourseMapper.insert(uc);
        return Result.success("加入成功");
    }

    @Override
    public PageResult<Course> listJoinedCourses(User user, int page, int pageSize) {
        LambdaQueryWrapper<UserCourse> ucWrapper = new LambdaQueryWrapper<>();
        ucWrapper.eq(UserCourse::getStudentId, user.getUserId());
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
        return Result.success(course);
    }

    /** 生成6位选课码 */
    private String generateCourseCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
