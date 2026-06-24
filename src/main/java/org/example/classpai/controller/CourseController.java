package org.example.classpai.controller;

import jakarta.servlet.http.HttpServletRequest;
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

    /** 创建课程（教师） */
    @PostMapping
    public Result<Course> create(@RequestBody CourseDTO dto, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.createCourse(dto, user);
    }

    /** 我的课程（教师） */
    @GetMapping("/my")
    public PageResult<Course> myCourses(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.listMyCourses(user, page, pageSize);
    }

    /** 通过选课码加入课程（学生/教师均可用） */
    @PostMapping("/join")
    public Result<?> join(@RequestParam String courseCode, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.joinCourse(courseCode, user);
    }

    /** 已加入的课程（学生） */
    @GetMapping("/joined")
    public PageResult<Course> joinedCourses(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.listJoinedCourses(user, page, pageSize);
    }

    /** 课程详情（仅课程成员可查看） */
    @GetMapping("/{courseId}")
    public Result<Course> detail(@PathVariable Long courseId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return courseService.getCourseDetail(courseId, user);
    }
}
