package org.example.classpai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.entity.UserCourse;
import org.example.classpai.mapper.CourseMapper;
import org.example.classpai.mapper.UserCourseMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 课程相关服务的公共基类 —— 统一权限校验
 */
public abstract class BaseCourseServiceImpl {

    @Autowired
    protected UserCourseMapper userCourseMapper;

    @Autowired
    protected CourseMapper courseMapper;

    /** 校验课程成员，返回关联记录 */
    protected UserCourse checkMembership(Long courseId, Long userId) {
        UserCourse uc = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getCourseId, courseId)
                        .eq(UserCourse::getUserId, userId));
        if (uc == null) {
            throw new BusinessException(403, "非课程成员");
        }
        return uc;
    }

    /** 校验是否为教师（不是则抛异常） */
    protected void checkTeacher(Long courseId, Long userId) {
        UserCourse uc = checkMembership(courseId, userId);
        if (!"teacher".equals(uc.getRole())) {
            throw new BusinessException(403, "仅教师可操作");
        }
    }

    /** 判断是否为教师（不抛异常） */
    protected boolean isTeacher(Long courseId, Long userId) {
        UserCourse uc = checkMembership(courseId, userId);
        return "teacher".equals(uc.getRole());
    }
}
