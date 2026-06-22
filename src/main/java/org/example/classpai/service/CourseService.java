package org.example.classpai.service;

import org.example.classpai.common.PageResult;
import org.example.classpai.common.Result;
import org.example.classpai.dto.CourseDTO;
import org.example.classpai.entity.Course;
import org.example.classpai.entity.User;

public interface CourseService {

    /** 教师创建课程 */
    Result<Course> createCourse(CourseDTO dto, User teacher);

    /** 教师查看自己的课程列表 */
    PageResult<Course> listMyCourses(User teacher, int page, int pageSize);

    /** 学生通过课程码加入课程 */
    Result<?> joinCourse(String courseCode, User student);

    /** 学生查看已选课程 */
    PageResult<Course> listJoinedCourses(User student, int page, int pageSize);

    /** 获取课程详情 */
    Result<Course> getCourseDetail(Long courseId, User user);
}
