package org.example.classpai.service;

import org.example.classpai.common.PageResult;
import org.example.classpai.common.Result;
import org.example.classpai.dto.CourseDTO;
import org.example.classpai.entity.Course;
import org.example.classpai.entity.User;

public interface CourseService {

    /** 创建课程（仅教师可用） */
    Result<Course> createCourse(CourseDTO dto, User user);

    /** 教师查看自己教的课程 */
    PageResult<Course> listMyCourses(User user, int page, int pageSize);

    /** 通过选课码加入课程（学生→course_student，教师→teacher_course） */
    Result<?> joinCourse(String courseCode, User user);

    /** 学生查看已加入的课程 */
    PageResult<Course> listJoinedCourses(User user, int page, int pageSize);

    /** 获取课程详情（仅课程成员） */
    Result<Course> getCourseDetail(Long courseId, User user);
}
