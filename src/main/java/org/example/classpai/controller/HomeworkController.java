package org.example.classpai.controller;

import jakarta.servlet.http.HttpSession;
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

    /** 教师布置作业 */
    @PostMapping("/course/{courseId}")
    public Result<Homework> create(@PathVariable Long courseId,
                                   @RequestBody HomeworkDTO dto,
                                   HttpSession session) {
        User user = (User) session.getAttribute("user");
        return homeworkService.createHomework(courseId, dto, user);
    }

    /** 查看课程作业列表 */
    @GetMapping("/course/{courseId}")
    public PageResult<Homework> list(@PathVariable Long courseId,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int pageSize) {
        return homeworkService.listHomework(courseId, page, pageSize);
    }

    /** 学生提交作业 */
    @PostMapping("/{homeworkId}/submit")
    public Result<Submission> submit(@PathVariable Long homeworkId,
                                      @RequestBody SubmissionDTO dto,
                                      HttpSession session) {
        User user = (User) session.getAttribute("user");
        return homeworkService.submit(homeworkId, dto, user);
    }

    /** 教师查看某作业的所有提交 */
    @GetMapping("/{homeworkId}/submissions")
    public PageResult<Submission> submissions(@PathVariable Long homeworkId,
                                               HttpSession session,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute("user");
        return homeworkService.listSubmissions(homeworkId, user, page, pageSize);
    }

    /** 教师批改评分 */
    @PutMapping("/submission/{submissionId}/grade")
    public Result<?> grade(@PathVariable Long submissionId,
                           @RequestBody GradeDTO dto,
                           HttpSession session) {
        User user = (User) session.getAttribute("user");
        return homeworkService.grade(submissionId, dto, user);
    }
}
