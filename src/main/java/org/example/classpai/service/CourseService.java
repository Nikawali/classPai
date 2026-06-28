package org.example.classpai.service;

import org.example.classpai.common.Result;
import org.example.classpai.dto.CourseDTO;
import org.example.classpai.dto.MemberDTO;
import org.example.classpai.dto.UserAllCoursesDTO;
import org.example.classpai.entity.Course;
import org.example.classpai.entity.User;

import java.util.List;

public interface CourseService {

    Result<Course> createCourse(CourseDTO dto, User user);

    Result<?> joinCourse(String courseCode, User user);

    Result<Course> getCourseDetail(Long courseId, User user);

    /** 获取课程所有成员 */
    Result<List<MemberDTO>> getCourseMembers(Long courseId, User user);

    /** 切换置顶状态 */
    Result<?> togglePin(Long courseId, User user);

    /** 更新置顶排序 */
    Result<?> updatePinnedOrder(List<Long> courseIds, User user);

    /** 一次性获取用户的所有课程数据（置顶 + 分组） */
    Result<UserAllCoursesDTO> getAllCourses(User user);

    /** 学生退出课程 */
    Result<?> quitCourse(Long courseId, User user);

    /** 归档课程（用户级，从主页隐藏但保留数据） */
    Result<?> archiveCourse(Long courseId, User user);

    /** 取消归档，课程恢复至主页 */
    Result<?> unarchiveCourse(Long courseId, User user);

    /** 获取用户已归档的课程列表 */
    Result<List<Course>> getArchivedCourses(User user);
}
