package org.example.classpai.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.classpai.common.PageResult;
import org.example.classpai.common.Result;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.dto.GradeDTO;
import org.example.classpai.dto.HomeworkDTO;
import org.example.classpai.dto.SubmissionDTO;
import org.example.classpai.entity.*;
import org.example.classpai.mapper.*;
import org.example.classpai.service.HomeworkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class HomeworkServiceImpl implements HomeworkService {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "homework";

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;

    private final HomeworkMapper homeworkMapper;
    private final SubmissionMapper submissionMapper;
    private final UserCourseMapper userCourseMapper;
    private final HomeworkFileMapper homeworkFileMapper;
    private final RestTemplate restTemplate;
    private final CourseMapper courseMapper;

    public HomeworkServiceImpl(HomeworkMapper homeworkMapper,
                               SubmissionMapper submissionMapper,
                               UserCourseMapper userCourseMapper,
                               HomeworkFileMapper homeworkFileMapper,
                               RestTemplate restTemplate,
                               CourseMapper courseMapper) {
        this.homeworkMapper = homeworkMapper;
        this.submissionMapper = submissionMapper;
        this.userCourseMapper = userCourseMapper;
        this.homeworkFileMapper = homeworkFileMapper;
        this.restTemplate = restTemplate;
        this.courseMapper = courseMapper;
    }

    private void checkTeacher(Long courseId, Long userId) {
        LambdaQueryWrapper<UserCourse> w = new LambdaQueryWrapper<>();
        w.eq(UserCourse::getCourseId, courseId)
         .eq(UserCourse::getUserId, userId)
         .eq(UserCourse::getRole, "teacher");
        if (userCourseMapper.selectCount(w) == 0) {
            throw new BusinessException(403, "无权操作");
        }
    }

    @Override
    public Result<Homework> createHomework(Long courseId, HomeworkDTO dto, MultipartFile[] files, User user) {
        checkTeacher(courseId, user.getUserId());
        Homework hw = new Homework();
        hw.setCourseId(courseId);
        hw.setTitle(dto.getTitle());
        hw.setContent(dto.getContent());
        if (dto.getStartTime() != null) {
            hw.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(dto.getStartTime()), ZoneId.systemDefault()));
        }
        if (dto.getDeadline() != null) {
            hw.setDeadline(LocalDateTime.ofInstant(Instant.ofEpochSecond(dto.getDeadline()), ZoneId.systemDefault()));
        }
        homeworkMapper.insert(hw);
        if (files != null && files.length > 0) {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                try {
                    String originalName = file.getOriginalFilename();
                    String ext = "";
                    if (originalName != null && originalName.contains(".")) {
                        ext = originalName.substring(originalName.lastIndexOf("."));
                    }
                    String newName = UUID.randomUUID() + ext;
                    Path dest = Paths.get(UPLOAD_DIR, newName);
                    Files.copy(file.getInputStream(), dest);
                    HomeworkFile hf = new HomeworkFile();
                    hf.setHwId(hw.getHwId());
                    hf.setFilePath("/uploads/homework/" + newName);
                    hf.setFileSize(file.getSize());
                    hf.setFileType(ext.isEmpty() ? null : ext.replace(".", ""));
                    hf.setUploadTime(LocalDateTime.now());
                    homeworkFileMapper.insert(hf);
                } catch (IOException e) {
                    throw new BusinessException(500, "文件保存失败: " + e.getMessage());
                }
            }
        }
        return Result.success(hw);
    }

    @Override
    public PageResult<Homework> listHomework(Long courseId, User user, int page, int pageSize) {
        LambdaQueryWrapper<Homework> w = new LambdaQueryWrapper<>();
        w.eq(Homework::getCourseId, courseId).orderByDesc(Homework::getCreateTime);
        Page<Homework> p = homeworkMapper.selectPage(new Page<>(page, pageSize), w);
        List<Homework> records = p.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(p.getTotal(), records, p.getCurrent(), p.getSize());
        }
        List<Long> hwIds = records.stream().map(Homework::getHwId).collect(Collectors.toList());
        String courseRole = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getCourseId, courseId)
                        .eq(UserCourse::getUserId, user.getUserId())).getRole();
        boolean isTeacher = "teacher".equalsIgnoreCase(courseRole);
        if (isTeacher) {
            long totalStudents = userCourseMapper.selectCount(
                    new LambdaQueryWrapper<UserCourse>()
                            .eq(UserCourse::getCourseId, courseId)
                            .eq(UserCourse::getRole, "student"));
            Map<Long, Integer> submitMap = submissionMapper.countSubmitByHwIds(hwIds).stream()
                    .collect(Collectors.toMap(
                            m -> ((Number) m.get("hw_id")).longValue(),
                            m -> ((Number) m.get("total")).intValue()));
            Map<Long, Integer> gradedMap = submissionMapper.countGradedByHwIds(hwIds).stream()
                    .collect(Collectors.toMap(
                            m -> ((Number) m.get("hw_id")).longValue(),
                            m -> ((Number) m.get("total")).intValue()));
            for (Homework hw : records) {
                hw.setTotalStudents((int) totalStudents);
                hw.setSubmitCount(submitMap.getOrDefault(hw.getHwId(), 0));
                hw.setGradedCount(gradedMap.getOrDefault(hw.getHwId(), 0));
            }
        } else {
            Set<Long> submittedSet = new HashSet<>(submissionMapper.findSubmittedHwIds(hwIds, user.getUserId()));
            for (Homework hw : records) {
                hw.setSubmitted(submittedSet.contains(hw.getHwId()));
            }
        }
        return PageResult.of(p.getTotal(), records, p.getCurrent(), p.getSize());
    }

    @Override
    public Result<Submission> submit(Long hwId, SubmissionDTO dto, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) throw new BusinessException(404, "作业不存在");
        Long submittedCount = submissionMapper.selectCount(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getHwId, hwId)
                        .eq(Submission::getStudentId, user.getUserId()));
        if (hw.getMaxSubmissions() != null && submittedCount >= hw.getMaxSubmissions()) {
            throw new BusinessException(400, "已达到最大提交次数（" + hw.getMaxSubmissions() + "次）");
        }
        Submission sub = new Submission();
        sub.setHwId(hwId);
        sub.setStudentId(user.getUserId());
        sub.setSubmitContent(dto.getContent());
        submissionMapper.insert(sub);
        return Result.success(sub);
    }

    @Override
    public PageResult<Submission> listSubmissions(Long hwId, User user, int page, int pageSize) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) throw new BusinessException(404, "作业不存在");
        checkTeacher(hw.getCourseId(), user.getUserId());
        LambdaQueryWrapper<Submission> w = new LambdaQueryWrapper<>();
        w.eq(Submission::getHwId, hwId).orderByAsc(Submission::getSubmitTime);
        Page<Submission> p = submissionMapper.selectPage(new Page<>(page, pageSize), w);
        return PageResult.of(p.getTotal(), p.getRecords(), p.getCurrent(), p.getSize());
    }

    @Override
    public Result<?> grade(Long submitId, GradeDTO dto, User user) {
        Submission sub = submissionMapper.selectById(submitId);
        if (sub == null) throw new BusinessException(404, "提交记录不存在");
        Homework hw = homeworkMapper.selectById(sub.getHwId());
        checkTeacher(hw.getCourseId(), user.getUserId());
        sub.setScore(dto.getScore());
        sub.setComment(dto.getComment());
        submissionMapper.updateById(sub);
        return Result.success("评分成功");
    }

    @Override
    public Result<List<Submission>> getGradingList(Long hwId, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) throw new BusinessException(404, "作业不存在");
        checkTeacher(hw.getCourseId(), user.getUserId());
        List<Submission> list = submissionMapper.findGradingListByHwId(hwId);
        int maxScore = hw.getTotalScore() != null ? hw.getTotalScore() : 100;
        for (Submission s : list) {
            if (s.getTotalScore() == null) s.setTotalScore(maxScore);
        }
        return Result.success(list);
    }

    @Override
    public Result<Homework> getHomework(Long hwId, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) throw new BusinessException(404, "作业不存在");
        Long count = userCourseMapper.selectCount(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getCourseId, hw.getCourseId())
                        .eq(UserCourse::getUserId, user.getUserId()));
        if (count == 0) throw new BusinessException(403, "非课程成员");
        return Result.success(hw);
    }

    @Override
    public Result<?> gradeByAI(Long submitId, User user) {
        Submission sub = submissionMapper.selectById(submitId);
        if (sub == null) throw new BusinessException(404, "提交记录不存在");
        Homework hw = homeworkMapper.selectById(sub.getHwId());
        checkTeacher(hw.getCourseId(), user.getUserId());
        JSONObject body = new JSONObject();
        body.put("model", "deepseek-chat");
        body.put("temperature", 0.2);
        body.put("stream", false);
        body.put("messages", buildHomeworkPrompt(hw.getContent(), sub.getSubmitContent(), hw.getTotalScore()));
        JSONObject response = sendToAI(body);
        JSONObject choice = response.getJSONArray("choices").getJSONObject(0);
        String content = choice.getJSONObject("message").getString("content");
        JSONObject result = JSONObject.parseObject(content);
        sub.setScore(result.getInteger("score"));
        sub.setComment(result.getString("comment"));
        submissionMapper.updateById(sub);
        return Result.success("评分成功");
    }

    private List<Map<String, String>> buildHomeworkPrompt(String question, String answer, Integer totalScore) {
        String systemPrompt = "你是严谨的作业批改老师，请按照要求批改学生作业：\n" +
                "1. 严格按照给定总分进行打分，分数为二位浮点数\n" +
                "2. 评分依据：答案正确性、知识点准确性、内容完整性、逻辑条理\n" +
                "3. 只输出JSON格式结果，不要多余开场白、不要多余解释\n" +
                "固定返回格式：{\"score\": 实际得分, \"comment\": \"简短批改评语，说明扣分原因\"}";
        String userPrompt = String.format("【本次作业总分：%d分】\n【题目】：%s\n\n【学生作答】：\n%s",
                totalScore, question, answer);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));
        return messages;
    }

    private JSONObject sendToAI(JSONObject body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> request = new HttpEntity<>(body.toJSONString(), headers);
        return JSONObject.parseObject(restTemplate.postForObject(apiUrl, request, String.class));
    }

    private void checkMembership(Long courseId, Long userId) {
        Long count = userCourseMapper.selectCount(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getCourseId, courseId)
                        .eq(UserCourse::getUserId, userId));
        if (count == 0) throw new BusinessException(403, "非课程成员");
    }

    private String calcSubmitStatus(Homework hw, List<Submission> submissions) {
        if (!submissions.isEmpty() && submissions.get(0).getScore() != null) return "graded";
        if (!submissions.isEmpty()) return "submitted";
        if (hw.getDeadline() != null && LocalDateTime.now().isAfter(hw.getDeadline())) return "overdue";
        return "unsubmitted";
    }

    @Override
    public Result<?> getStudentHomeworkDetail(Long hwId, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) throw new BusinessException(404, "作业不存在");
        checkMembership(hw.getCourseId(), user.getUserId());
        Course course = courseMapper.selectById(hw.getCourseId());
        List<HomeworkFile> files = homeworkFileMapper.selectList(
                new LambdaQueryWrapper<HomeworkFile>().eq(HomeworkFile::getHwId, hwId));
        List<Submission> submissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getHwId, hwId)
                        .eq(Submission::getStudentId, user.getUserId())
                        .orderByDesc(Submission::getSubmitTime));
        String submitStatus = calcSubmitStatus(hw, submissions);
        Double latestScore = null;
        if (!submissions.isEmpty() && submissions.get(0).getScore() != null) {
            latestScore = submissions.get(0).getScore().doubleValue();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("title", hw.getTitle());
        result.put("content", hw.getContent());
        result.put("courseName", course != null ? course.getCourseName() : "");
        result.put("startTime", hw.getStartTime());
        result.put("deadline", hw.getDeadline());
        result.put("totalScore", hw.getTotalScore() != null ? hw.getTotalScore() : 100);
        result.put("files", files);
        result.put("submissions", submissions);
        result.put("submitStatus", submitStatus);
        result.put("latestScore", latestScore);
        return Result.success(result);
    }

    @Override
    public Result<?> getSubmitPageData(Long hwId, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) throw new BusinessException(404, "作业不存在");
        checkMembership(hw.getCourseId(), user.getUserId());
        Course course = courseMapper.selectById(hw.getCourseId());
        List<HomeworkFile> files = homeworkFileMapper.selectList(
                new LambdaQueryWrapper<HomeworkFile>().eq(HomeworkFile::getHwId, hwId));
        List<Submission> submissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getHwId, hwId)
                        .eq(Submission::getStudentId, user.getUserId())
                        .orderByDesc(Submission::getSubmitTime));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("title", hw.getTitle());
        result.put("content", hw.getContent());
        result.put("courseName", course != null ? course.getCourseName() : "");
        result.put("startTime", hw.getStartTime());
        result.put("deadline", hw.getDeadline());
        result.put("totalScore", hw.getTotalScore() != null ? hw.getTotalScore() : 100);
        result.put("teacherFiles", files);
        result.put("submissions", submissions);
        result.put("submitStatus", calcSubmitStatus(hw, submissions));
        result.put("submittedCount", submissions.size());
        return Result.success(result);
    }
}
