package org.example.classpai.controller;

import jakarta.servlet.http.HttpServletRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.classpai.common.PageResult;
import org.example.classpai.common.Result;
import org.example.classpai.dto.GradeDTO;
import org.example.classpai.dto.HomeworkDTO;
import org.example.classpai.entity.Homework;
import org.example.classpai.entity.HomeworkFile;
import org.example.classpai.entity.Submission;
import org.example.classpai.entity.User;
import org.example.classpai.vo.HomeworkListVO;
import org.example.classpai.vo.StudentHomeworkVO;
import org.example.classpai.mapper.HomeworkFileMapper;
import org.example.classpai.service.HomeworkService;
import org.example.classpai.service.MessageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/homework")
public class HomeworkController {

    private final HomeworkService homeworkService;
    private final HomeworkFileMapper homeworkFileMapper;
    private final MessageService messageService;

    public HomeworkController(HomeworkService homeworkService,
                              HomeworkFileMapper homeworkFileMapper,
                              MessageService messageService) {
        this.homeworkService = homeworkService;
        this.homeworkFileMapper = homeworkFileMapper;
        this.messageService = messageService;
    }

    /** 教师布置作业（接收文件上传） */
    @PostMapping("/course/{courseId}")
    public Result<Homework> create(@PathVariable Long courseId,
                                   @RequestParam String title,
                                   @RequestParam String content,
                                   @RequestParam(required = false) Long startTime,
                                   @RequestParam(required = false) Long deadline,
                                   @RequestParam(required = false) Integer maxSubmissions,
                                   @RequestParam(required = false) Integer totalScore,
                                   @RequestParam(required = false) MultipartFile[] files,
                                   HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        HomeworkDTO dto = new HomeworkDTO();
        dto.setTitle(title);
        dto.setContent(content);
        dto.setStartTime(startTime);
        dto.setDeadline(deadline);
        dto.setMaxSubmissions(maxSubmissions);
        dto.setTotalScore(totalScore);
        return homeworkService.createHomework(courseId, dto, files, user);
    }

    @GetMapping("/course/{courseId}")
    public Result<PageResult<HomeworkListVO>> list(@PathVariable Long courseId,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        PageResult<HomeworkListVO> pr = homeworkService.listHomework(courseId, user, page, pageSize);
        return Result.success(pr);
    }

    /** 获取单个作业 */
    @GetMapping("/{hwId}")
    public Result<Homework> getHomework(@PathVariable Long hwId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.getHomework(hwId, user);
    }

    @GetMapping("/{hwId}/detail")
    public Result<StudentHomeworkVO> teacherDetail(@PathVariable Long hwId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.getTeacherHomeworkDetail(hwId, user);
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
    public Result<Submission> submit(@PathVariable Long hwId,
                                      @RequestParam(required = false) String content,
                                      @RequestParam(required = false) MultipartFile[] files,
                                      HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.submit(hwId, content, files, user);
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

    /** 教师批阅页：获取作业所有学生提交状态 */
    @GetMapping("/{hwId}/grading")
    public Result<List<Submission>> grading(@PathVariable Long hwId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.getGradingList(hwId, user);
    }

    /** 学生端：获取作业详情（含提交记录、文件、状态） */
    @GetMapping("/{hwId}/student")
    public Result<?> studentDetail(@PathVariable Long hwId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.getStudentHomeworkDetail(hwId, user);
    }

    /** 学生端：获取提交页数据 */
    @GetMapping("/{hwId}/submit-page")
    public Result<?> submitPage(@PathVariable Long hwId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.getSubmitPageData(hwId, user);
    }

    /** 催交单个学生 */
    @PostMapping("/{hwId}/urge/{studentId}")
    public Result<?> urge(@PathVariable Long hwId,
                          @PathVariable Long studentId,
                          HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return messageService.urge(hwId, studentId, user);
    }

    /** 一键催交所有未提交学生 */
    @PostMapping("/{hwId}/urge-all")
    public Result<?> urgeAll(@PathVariable Long hwId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return messageService.urgeAll(hwId, user);
    }

    /** AI 一键批改所有未批改的提交 */
    @PostMapping("/{hwId}/grade-ai-batch")
    public Result<?> gradeAIBatch(@PathVariable Long hwId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.gradeByAIBatch(hwId, user);
    }

    /** 教师修改作业时间 */
    @PutMapping("/{hwId}/time")
    public Result<?> updateTime(@PathVariable Long hwId,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long deadline,
            HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return homeworkService.updateHomeworkTime(hwId, startTime, deadline, user);
    }
}
