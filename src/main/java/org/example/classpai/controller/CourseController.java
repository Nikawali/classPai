package org.example.classpai.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.classpai.common.Result;
import org.example.classpai.dto.CourseDTO;
import org.example.classpai.dto.MemberDTO;
import org.example.classpai.dto.UserAllCoursesDTO;
import org.example.classpai.entity.Course;
import org.example.classpai.entity.User;
import org.example.classpai.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /** 创建课程（教师） */
    @PostMapping
    public Result<Course> create(@RequestBody CourseDTO dto, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.createCourse(dto, user);
    }

    /** 通过选课码加入课程（学生/教师均可用） */
    @PostMapping("/join")
    public Result<?> join(@RequestParam String courseCode, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.joinCourse(courseCode, user);
    }

    /** 课程详情（仅课程成员可查看） */
    @GetMapping("/{courseId}")
    public Result<Course> detail(@PathVariable Long courseId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.getCourseDetail(courseId, user);
    }

    /** 获取课程所有成员 */
    @GetMapping("/{courseId}/members")
    public Result<List<MemberDTO>> members(@PathVariable Long courseId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.getCourseMembers(courseId, user);
    }

    /** 切换置顶 */
    @PostMapping("/{courseId}/pin")
    public Result<?> togglePin(@PathVariable Long courseId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.togglePin(courseId, user);
    }

    /** 更新置顶排序 */
    @PutMapping("/pinned/order")
    public Result<?> updatePinnedOrder(@RequestBody List<Long> courseIds, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.updatePinnedOrder(courseIds, user);
    }

    /** 一次性获取所有课程数据（置顶 + 分组，含角色/置顶/人数信息） */
    @GetMapping("/all")
    public Result<UserAllCoursesDTO> allCourses(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.getAllCourses(user);
    }

    /** 学生退出课程 */
    @DeleteMapping("/{courseId}/quit")
    public Result<?> quit(@PathVariable Long courseId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.quitCourse(courseId, user);
    }

    /** 归档课程 */
    @PostMapping("/{courseId}/archive")
    public Result<?> archive(@PathVariable Long courseId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.archiveCourse(courseId, user);
    }

    /** 取消归档 */
    @PostMapping("/{courseId}/unarchive")
    public Result<?> unarchive(@PathVariable Long courseId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.unarchiveCourse(courseId, user);
    }

    /** 获取已归档课程列表 */
    @GetMapping("/archived")
    public Result<List<Course>> archived(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.getArchivedCourses(user);
    }

    /** 教师修改课程成员角色 */
    @PutMapping("/{courseId}/members/{userId}/role")
    public Result<?> changeRole(@PathVariable Long courseId, @PathVariable Long userId,
            @RequestParam String role, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.changeMemberRole(courseId, userId, role, user);
    }
}
