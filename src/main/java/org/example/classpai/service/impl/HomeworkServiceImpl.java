package org.example.classpai.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.classpai.common.PageResult;
import org.example.classpai.common.Result;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.dto.*;
import org.example.classpai.entity.*;
import org.example.classpai.mapper.*;
import org.example.classpai.service.HomeworkService;
import org.example.classpai.vo.HomeworkListVO;
import org.example.classpai.vo.StudentHomeworkVO;
import org.springframework.beans.BeanUtils;
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

    private static final String UPLOAD_BASE = System.getProperty("user.dir") + File.separator + "uploads";

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;

    private final HomeworkMapper homeworkMapper;
    private final SubmissionMapper submissionMapper;
    private final UserCourseMapper userCourseMapper;
    private final HomeworkFileMapper homeworkFileMapper;
    private final SubmitFileMapper submitFileMapper;
    private final RestTemplate restTemplate;
    private final CourseMapper courseMapper;

    public HomeworkServiceImpl(HomeworkMapper homeworkMapper,
                               SubmissionMapper submissionMapper,
                               UserCourseMapper userCourseMapper,
                               HomeworkFileMapper homeworkFileMapper,
                               SubmitFileMapper submitFileMapper,
                               RestTemplate restTemplate,
                               CourseMapper courseMapper) {
        this.homeworkMapper = homeworkMapper;
        this.submissionMapper = submissionMapper;
        this.userCourseMapper = userCourseMapper;
        this.homeworkFileMapper = homeworkFileMapper;
        this.submitFileMapper = submitFileMapper;
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

    @FunctionalInterface
    private interface FileEntitySaver {
        void save(String newName, String filePath, long fileSize, String fileType);
    }

    /**
     * 保存上传文件到磁盘并回调创建对应实体记录
     * @param files  上传文件数组
     * @param subDir 子目录名，如 "homework"、"submit"
     * @param saver  实体创建回调（接收 newName, filePath, fileSize, fileType）
     */
    private void saveFiles(MultipartFile[] files, String subDir, FileEntitySaver saver) {
        if (files == null || files.length == 0) return;
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            try {
                String dirPath = UPLOAD_BASE + File.separator + subDir;
                File dir = new File(dirPath);
                if (!dir.exists()) dir.mkdirs();
                String originalName = file.getOriginalFilename();
                String ext = "";
                if (originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf("."));
                }
                String newName = UUID.randomUUID() + ext;
                Path dest = Paths.get(dirPath, newName);
                Files.copy(file.getInputStream(), dest);
                String filePath = "/uploads/" + subDir + "/" + newName;
                String fileType = ext.isEmpty() ? null : ext.replace(".", "");
                saver.save(newName, filePath, file.getSize(), fileType);
            } catch (IOException e) {
                throw new BusinessException(500, "文件保存失败: " + e.getMessage());
            }
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
        saveFiles(files, "homework", (newName, filePath, fileSize, fileType) -> {
            HomeworkFile hf = new HomeworkFile();
            hf.setHwId(hw.getHwId());
            hf.setFilePath(filePath);
            hf.setFileSize(fileSize);
            hf.setFileType(fileType);
            hf.setUploadTime(LocalDateTime.now());
            homeworkFileMapper.insert(hf);
        });
        return Result.success(hw);
    }

    @Override
    public PageResult<HomeworkListVO> listHomework(Long courseId, User user, int page, int pageSize) {
        LambdaQueryWrapper<Homework> w = new LambdaQueryWrapper<>();
        w.eq(Homework::getCourseId, courseId).orderByDesc(Homework::getCreateTime);
        Page<Homework> p = homeworkMapper.selectPage(new Page<>(page, pageSize), w);
        List<Homework> records = p.getRecords();
        List<HomeworkListVO> voList = records.stream().map(hw -> {
            HomeworkListVO vo = new HomeworkListVO();
            BeanUtils.copyProperties(hw, vo);
            return vo;
        }).collect(Collectors.toList());
        if (voList.isEmpty()) {
            return PageResult.of(p.getTotal(), voList, p.getCurrent(), p.getSize());
        }
        List<Long> hwIds = voList.stream().map(HomeworkListVO::getHwId).collect(Collectors.toList());
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
            for (HomeworkListVO vo : voList) {
                vo.setTotalStudents((int) totalStudents);
                vo.setSubmitCount(submitMap.getOrDefault(vo.getHwId(), 0));
                vo.setGradedCount(gradedMap.getOrDefault(vo.getHwId(), 0));
            }
        } else {
            Set<Long> submittedSet = new HashSet<>(submissionMapper.findSubmittedHwIds(hwIds, user.getUserId()));
            for (HomeworkListVO vo : voList) {
                vo.setSubmitted(submittedSet.contains(vo.getHwId()));
            }
        }
        return PageResult.of(p.getTotal(), voList, p.getCurrent(), p.getSize());
    }

    @Override
    public Result<Submission> submit(Long hwId, String content, MultipartFile[] files, User user) {
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
        sub.setSubmitContent(content);
        submissionMapper.insert(sub);
        saveFiles(files, "submit", (newName, filePath, fileSize, fileType) -> {
            SubmitFile sf = new SubmitFile();
            sf.setSubmitId(sub.getSubmitId());
            sf.setFilePath(filePath);
            sf.setFileSize(fileSize);
            sf.setFileType(fileType);
            sf.setUploadTime(LocalDateTime.now());
            submitFileMapper.insert(sf);
        });
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
    public Result<StudentHomeworkVO> getStudentHomeworkDetail(Long hwId, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) throw new BusinessException(404, "作业不存在");
        checkMembership(hw.getCourseId(), user.getUserId());
        Course course = courseMapper.selectById(hw.getCourseId());
        StudentHomeworkVO vo = new StudentHomeworkVO();
        BeanUtils.copyProperties(hw, vo);
        vo.setCourseName(course != null ? course.getCourseName() : "");
        vo.setTotalScore(hw.getTotalScore() != null ? hw.getTotalScore() : 100);
        vo.setFiles(homeworkFileMapper.selectList(
                new LambdaQueryWrapper<HomeworkFile>().eq(HomeworkFile::getHwId, hwId)));
        for (HomeworkFile f : vo.getFiles()) {
            String name = f.getFilePath() != null ? f.getFilePath().substring(f.getFilePath().lastIndexOf("/") + 1) : "";
            f.setOriginalName(name);
            f.setFileName(name);
        }
        vo.setSubmissions(submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getHwId, hwId)
                        .eq(Submission::getStudentId, user.getUserId())
                        .orderByDesc(Submission::getSubmitTime)));
        vo.setSubmitStatus(calcSubmitStatus(hw, vo.getSubmissions()));
        if (!vo.getSubmissions().isEmpty() && vo.getSubmissions().get(0).getScore() != null) {
            vo.setLatestScore(vo.getSubmissions().get(0).getScore().doubleValue());
        }
        return Result.success(vo);
    }

    @Override
    public Result<StudentHomeworkVO> getSubmitPageData(Long hwId, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) throw new BusinessException(404, "作业不存在");
        checkMembership(hw.getCourseId(), user.getUserId());
        Course course = courseMapper.selectById(hw.getCourseId());
        StudentHomeworkVO vo = new StudentHomeworkVO();
        BeanUtils.copyProperties(hw, vo);
        vo.setCourseName(course != null ? course.getCourseName() : "");
        vo.setTotalScore(hw.getTotalScore() != null ? hw.getTotalScore() : 100);
        vo.setFiles(homeworkFileMapper.selectList(
                new LambdaQueryWrapper<HomeworkFile>().eq(HomeworkFile::getHwId, hwId)));
        for (HomeworkFile f : vo.getFiles()) {
            String name = f.getFilePath() != null ? f.getFilePath().substring(f.getFilePath().lastIndexOf("/") + 1) : "";
            f.setOriginalName(name);
            f.setFileName(name);
        }
        vo.setSubmissions(submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getHwId, hwId)
                        .eq(Submission::getStudentId, user.getUserId())
                        .orderByDesc(Submission::getSubmitTime)));
        vo.setSubmitStatus(calcSubmitStatus(hw, vo.getSubmissions()));
        vo.setSubmittedCount(vo.getSubmissions().size());
        return Result.success(vo);
    }
}
