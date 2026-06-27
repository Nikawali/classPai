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
    private String API_URL;
    @Value("${deepseek.api.key}")
    private String API_KEY;

    private final HomeworkMapper homeworkMapper;
    private final SubmissionMapper submissionMapper;
    private final UserCourseMapper userCourseMapper;
    private final HomeworkFileMapper homeworkFileMapper;
    private final RestTemplate restTemplate;

    public HomeworkServiceImpl(HomeworkMapper homeworkMapper,
                               SubmissionMapper submissionMapper,
                               UserCourseMapper userCourseMapper,
                               HomeworkFileMapper homeworkFileMapper,
                               RestTemplate restTemplate) {
        this.homeworkMapper = homeworkMapper;
        this.submissionMapper = submissionMapper;
        this.userCourseMapper = userCourseMapper;
        this.homeworkFileMapper = homeworkFileMapper;
        this.restTemplate=restTemplate;
    }

    /** 校验用户是否该课程教师 */
    private void checkTeacher(Long courseId, Long userId) {
        LambdaQueryWrapper<UserCourse> w = new LambdaQueryWrapper<>();
        w.eq(UserCourse::getCourseId, courseId)
         .eq(UserCourse::getUserId, userId)
         .eq(UserCourse::getRole, "teacher");
        if (userCourseMapper.selectCount(w) == 0) {
            throw new BusinessException(403, "无权操作");
        }
    }

    /** 【教师布置作业】创建作业主体信息 + 上传附件文件到服务器并记录到 homework_file 表 */
    @Override
    public Result<Homework> createHomework(Long courseId, HomeworkDTO dto, MultipartFile[] files, User user) {
        checkTeacher(courseId, user.getUserId());

        // 1. 写入 homework 表
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

        // 2. 保存文件到服务器，写入 homework_file 表
        if (files != null && files.length > 0) {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                try {
                    // 提取文件信息
                    String originalName = file.getOriginalFilename();
                    String ext = "";
                    if (originalName != null && originalName.contains(".")) {
                        ext = originalName.substring(originalName.lastIndexOf("."));
                    }
                    String newName = UUID.randomUUID() + ext;
                    Path dest = Paths.get(UPLOAD_DIR, newName);
                    Files.copy(file.getInputStream(), dest);

                    // 写入 homework_file 表
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

    /** 【课程详情-作业列表】分页查询某课程作业，教师视角：已交/已批/总人数；学生视角：是否已提交 */
    @Override
    public PageResult<Homework> listHomework(Long courseId, User user, int page, int pageSize) {
        LambdaQueryWrapper<Homework> w = new LambdaQueryWrapper<>();
        w.eq(Homework::getCourseId, courseId)
         .orderByDesc(Homework::getCreateTime);
        Page<Homework> p = homeworkMapper.selectPage(new Page<>(page, pageSize), w);
        List<Homework> records = p.getRecords();

        if (records.isEmpty()) {
            return PageResult.of(p.getTotal(), records, p.getCurrent(), p.getSize());
        }

        List<Long> hwIds = records.stream().map(Homework::getHwId).collect(Collectors.toList());

        // 查用户在当前课程中的角色
        String courseRole = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getCourseId, courseId)
                        .eq(UserCourse::getUserId, user.getUserId()))
                .getRole();
        boolean isTeacher = "teacher".equalsIgnoreCase(courseRole);

        if (isTeacher) {
            // 教师：统计课程总学生人数
            long totalStudents = userCourseMapper.selectCount(
                    new LambdaQueryWrapper<UserCourse>()
                            .eq(UserCourse::getCourseId, courseId)
                            .eq(UserCourse::getRole, "student"));

            // 已交人数
            Map<Long, Integer> submitMap = submissionMapper.countSubmitByHwIds(hwIds).stream()
                    .collect(Collectors.toMap(
                            m -> ((Number) m.get("hw_id")).longValue(),
                            m -> ((Number) m.get("total")).intValue()));

            // 已批人数（score IS NOT NULL）
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
            // 学生：查哪些作业已提交
            Set<Long> submittedSet = new HashSet<>(submissionMapper.findSubmittedHwIds(hwIds, user.getUserId()));
            for (Homework hw : records) {
                hw.setSubmitted(submittedSet.contains(hw.getHwId()));
            }
        }

        return PageResult.of(p.getTotal(), records, p.getCurrent(), p.getSize());
    }

    /** 【学生提交作业】提交作业文本内容，已提交则覆盖更新 */
    @Override
    public Result<Submission> submit(Long hwId, SubmissionDTO dto, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) {
            throw new BusinessException(404, "作业不存在");
        }

        // 查重：同学生对同作业只保留一份
        LambdaQueryWrapper<Submission> w = new LambdaQueryWrapper<>();
        w.eq(Submission::getHwId, hwId)
         .eq(Submission::getStudentId, user.getUserId());
        Submission exist = submissionMapper.selectOne(w);
        if (exist != null) {
            exist.setSubmitContent(dto.getContent());
            submissionMapper.updateById(exist);
            return Result.success(exist);
        }

        Submission sub = new Submission();
        sub.setHwId(hwId);
        sub.setStudentId(user.getUserId());
        sub.setSubmitContent(dto.getContent());
        submissionMapper.insert(sub);
        return Result.success(sub);
    }

    /** 【教师查看提交列表】分页查询某作业所有已提交记录（仅已提交的学生） */
    @Override
    public PageResult<Submission> listSubmissions(Long hwId, User user,
                                                   int page, int pageSize) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) throw new BusinessException(404, "作业不存在");
        checkTeacher(hw.getCourseId(), user.getUserId());

        LambdaQueryWrapper<Submission> w = new LambdaQueryWrapper<>();
        w.eq(Submission::getHwId, hwId)
         .orderByAsc(Submission::getSubmitTime);
        Page<Submission> p = submissionMapper.selectPage(new Page<>(page, pageSize), w);
        return PageResult.of(p.getTotal(), p.getRecords(), p.getCurrent(), p.getSize());
    }

    /** 【教师批改评分】对某份提交记录打分并写评语 */
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

    /** 【教师批阅页】获取课程全体学生对该作业的提交状态（含未交、已交未批、已批三种状态） */
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

    /** 【作业详情】按 ID 获取单个作业基本信息，需课程成员权限 */
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
    public Result<?> gradeByAI(Long submitId,  User user) {
        Submission sub = submissionMapper.selectById(submitId);
        if (sub == null) throw new BusinessException(404, "提交记录不存在");

        Homework hw = homeworkMapper.selectById(sub.getHwId());
        checkTeacher(hw.getCourseId(), user.getUserId());

        JSONObject body=new JSONObject();
        body.put("model","deepseek-chat");
        body.put("temperature",0.2);
        body.put("stream",false);
        body.put("messages", buildHomeworkPrompt(hw.getContent(), sub.getSubmitContent(),hw.getTotalScore()));

        JSONObject response = sendToAI(body);
        //数据在choices[0].message.content
        JSONObject choice = response.getJSONArray("choices").getJSONObject(0);
        String content = choice.getJSONObject("message").getString("content");

        JSONObject result = JSONObject.parseObject(content);

        sub.setScore(result.getInteger("score"));
        sub.setComment(result.getString("comment"));
        submissionMapper.updateById(sub);
        submissionMapper.updateById(sub);
        return Result.success("评分成功");
    }

    private List<Map<String, String>> buildHomeworkPrompt(String question, String answer, Integer totalScore) {
        // 系统提示词：批改规则 + 输出约束
        String systemPrompt = """
    你是严谨的作业批改老师，请按照要求批改学生作业：
    1. 严格按照给定总分进行打分，分数为二位浮点数
    2. 评分依据：答案正确性、知识点准确性、内容完整性、逻辑条理
    3. 只输出JSON格式结果，不要多余开场白、不要多余解释
    固定返回格式：{"score": 实际得分, "comment": "简短批改评语，说明扣分原因"}
    """;

        // 用户结构化内容：总分、题目、作答
        String userPrompt = String.format("""
    【本次作业总分：%d分】
    【题目】：%s

    【学生作答】：
    %s
    """, totalScore, question, answer);

        // 组装messages
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));

        return messages;
    }

    private JSONObject sendToAI(JSONObject body){
        HttpHeaders headers=new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer"+API_KEY);
        HttpEntity<String> request=new HttpEntity<>(body.toJSONString(),headers);
        return JSONObject.parseObject(restTemplate.postForObject(API_URL,request,String.class));
    }
}
