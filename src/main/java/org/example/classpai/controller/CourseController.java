package org.example.classpai.controller;

import jakarta.servlet.http.HttpSession;
import org.example.classpai.common.PageResult;
import org.example.classpai.common.Result;
import org.example.classpai.dto.CourseDTO;
import org.example.classpai.entity.Course;
import org.example.classpai.entity.User;
import org.example.classpai.service.CourseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /** 教师创建课程 */
    @PostMapping
    public Result<Course> create(@RequestBody CourseDTO dto, HttpSession session) {
        User user = (User) session.getAttribute("user");
        return courseService.createCourse(dto, user);
    }

    /** 教师查看自己的课程 */
    @GetMapping("/my")
    public PageResult<Course> myCourses(HttpSession session,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute("user");
        return courseService.listMyCourses(user, page, pageSize);
    }

    /** 学生通过课程码加入课程 */
    @PostMapping("/join")
    public Result<?> join(@RequestParam String courseCode, HttpSession session) {
        User user = (User) session.getAttribute("user");
        return courseService.joinCourse(courseCode, user);
    }

    /** 学生查看已选课程 */
    @GetMapping("/joined")
    public PageResult<Course> joinedCourses(HttpSession session,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute("user");
        return courseService.listJoinedCourses(user, page, pageSize);
    }

    /** 获取课程详情 */
    @GetMapping("/{courseId}")
    public Result<Course> detail(@PathVariable Long courseId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        return courseService.getCourseDetail(courseId, user);
    }
}
