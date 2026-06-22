package org.example.classpai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.classpai.common.PageResult;
import org.example.classpai.common.Result;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.dto.CourseDTO;
import org.example.classpai.entity.Course;
import org.example.classpai.entity.User;
import org.example.classpai.entity.UserCourse;
import org.example.classpai.mapper.CourseMapper;
import org.example.classpai.mapper.UserCourseMapper;
import org.example.classpai.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final UserCourseMapper userCourseMapper;

    public CourseServiceImpl(CourseMapper courseMapper, UserCourseMapper userCourseMapper) {
        this.courseMapper = courseMapper;
        this.userCourseMapper = userCourseMapper;
    }

    @Override
    @Transactional
    public Result<Course> createCourse(CourseDTO dto, User teacher) {
        Course course = new Course();
        course.setCourseName(dto.getCourseName());
        course.setDescription(dto.getDescription());
        course.setTeacherId(teacher.getId());
        course.setCourseCode(generateCourseCode());
        courseMapper.insert(course);
        return Result.success(course);
    }

    @Override
    public PageResult<Course> listMyCourses(User teacher, int page, int pageSize) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getTeacherId, teacher.getId())
               .orderByDesc(Course::getCreateTime);
        Page<Course> p = courseMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return PageResult.of(p.getTotal(), p.getRecords(), p.getCurrent(), p.getSize());
    }

    @Override
    @Transactional
    public Result<?> joinCourse(String courseCode, User student) {
        if (!"STUDENT".equals(student.getRole())) {
            throw new BusinessException(400, "仅学生可加入课程");
        }

        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.eq(Course::getCourseCode, courseCode);
        Course course = courseMapper.selectOne(courseWrapper);
        if (course == null) {
            throw new BusinessException(400, "课程码无效");
        }

        LambdaQueryWrapper<UserCourse> ucWrapper = new LambdaQueryWrapper<>();
        ucWrapper.eq(UserCourse::getUserId, student.getId())
                 .eq(UserCourse::getCourseId, course.getId());
        if (userCourseMapper.selectCount(ucWrapper) > 0) {
            throw new BusinessException(400, "已加入该课程");
        }

        UserCourse userCourse = new UserCourse();
        userCourse.setUserId(student.getId());
        userCourse.setCourseId(course.getId());
        userCourseMapper.insert(userCourse);
        return Result.success("加入成功");
    }

    @Override
    public PageResult<Course> listJoinedCourses(User student, int page, int pageSize) {
        LambdaQueryWrapper<UserCourse> ucWrapper = new LambdaQueryWrapper<>();
        ucWrapper.eq(UserCourse::getUserId, student.getId());
        List<Long> courseIds = userCourseMapper.selectList(ucWrapper)
                .stream().map(UserCourse::getCourseId).collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return PageResult.of(0, List.of(), page, pageSize);
        }

        Page<Course> p = courseMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Course>().in(Course::getId, courseIds)
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
