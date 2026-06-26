package org.example.classpai.controller;

import jakarta.servlet.http.HttpServletRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.classpai.common.PageResult;
import org.example.classpai.common.Result;
import org.example.classpai.dto.GradeDTO;
import org.example.classpai.dto.HomeworkDTO;
import org.example.classpai.dto.SubmissionDTO;
import org.example.classpai.entity.Homework;
import org.example.classpai.entity.HomeworkFile;
import org.example.classpai.entity.Submission;
import org.example.classpai.entity.User;
import org.example.classpai.mapper.HomeworkFileMapper;
import org.example.classpai.service.HomeworkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/homework")
public class HomeworkController {

    private final HomeworkService homeworkService;
    private final HomeworkFileMapper homeworkFileMapper;

    public HomeworkController(HomeworkService homeworkService,
                              HomeworkFileMapper homeworkFileMapper) {
        this.homeworkService = homeworkService;
        this.homeworkFileMapper = homeworkFileMapper;
    }

    @PostMapping("/course/{courseId}")
    public Result<Homework> create(@PathVariable Long courseId, @RequestBody HomeworkDTO dto,
                                    HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.createHomework(courseId, dto, user);
    }

    @GetMapping("/course/{courseId}")
    public Result<PageResult<Homework>> list(@PathVariable Long courseId,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<Homework> pr = homeworkService.listHomework(courseId, page, pageSize);
        return Result.success(pr);
    }

    /** 获取某个作业的文件列表 */
    @GetMapping("/{hwId}/files")
    public Result<List<HomeworkFile>> files(@PathVariable Long hwId) {
        List<HomeworkFile> files = homeworkFileMapper.selectList(
                new LambdaQueryWrapper<HomeworkFile>()
                        .eq(HomeworkFile::getHwId, hwId));
        return Result.success(files);
    }

    @PostMapping("/{hwId}/submit")
    public Result<Submission> submit(@PathVariable Long hwId, @RequestBody SubmissionDTO dto,
                                      HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.submit(hwId, dto, user);
    }

    @GetMapping("/{hwId}/submissions")
    public Result<PageResult<Submission>> submissions(@PathVariable Long hwId,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        PageResult<Submission> pr = homeworkService.listSubmissions(hwId, user, page, pageSize);
        return Result.success(pr);
    }

    @PutMapping("/submission/{submitId}/grade")
    public Result<?> grade(@PathVariable Long submitId, @RequestBody GradeDTO dto,
                            HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.grade(submitId, dto, user);
    }
}
