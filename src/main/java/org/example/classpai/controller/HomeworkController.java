package org.example.classpai.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.classpai.common.PageResult;
import org.example.classpai.common.Result;
import org.example.classpai.dto.GradeDTO;
import org.example.classpai.dto.HomeworkDTO;
import org.example.classpai.dto.SubmissionDTO;
import org.example.classpai.entity.Homework;
import org.example.classpai.entity.Submission;
import org.example.classpai.entity.User;
import org.example.classpai.service.HomeworkService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/homework")
public class HomeworkController {

    private final HomeworkService homeworkService;

    public HomeworkController(HomeworkService homeworkService) {
        this.homeworkService = homeworkService;
    }

    @PostMapping("/course/{courseId}")
    public Result<Homework> create(@PathVariable Long courseId, @RequestBody HomeworkDTO dto,
                                    HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.createHomework(courseId, dto, user);
    }

    @GetMapping("/course/{courseId}")
    public PageResult<Homework> list(@PathVariable Long courseId,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int pageSize) {
        return homeworkService.listHomework(courseId, page, pageSize);
    }

    @PostMapping("/{hwId}/submit")
    public Result<Submission> submit(@PathVariable Long hwId, @RequestBody SubmissionDTO dto,
                                      HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.submit(hwId, dto, user);
    }

    @GetMapping("/{hwId}/submissions")
    public PageResult<Submission> submissions(@PathVariable Long hwId,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.listSubmissions(hwId, user, page, pageSize);
    }

    @PutMapping("/submission/{submitId}/grade")
    public Result<?> grade(@PathVariable Long submitId, @RequestBody GradeDTO dto,
                            HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.grade(submitId, dto, user);
    }
}
