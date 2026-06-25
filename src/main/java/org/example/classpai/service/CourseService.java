package org.example.classpai.service;

import org.example.classpai.common.Result;
import org.example.classpai.dto.CourseDTO;
import org.example.classpai.dto.UserAllCoursesDTO;
import org.example.classpai.entity.Course;
import org.example.classpai.entity.User;

import java.util.List;

public interface CourseService {

    Result<Course> createCourse(CourseDTO dto, User user);

    Result<?> joinCourse(String courseCode, User user);

    Result<Course> getCourseDetail(Long courseId, User user);

    /** 切换置顶状态 */
    Result<?> togglePin(Long courseId, User user);

    /** 更新置顶排序 */
    Result<?> updatePinnedOrder(List<Long> courseIds, User user);

    /** 一次性获取用户的所有课程数据（置顶 + 分组） */
    Result<UserAllCoursesDTO> getAllCourses(User user);
}
