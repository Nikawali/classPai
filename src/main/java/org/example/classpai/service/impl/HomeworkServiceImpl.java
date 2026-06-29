package org.example.classpai.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.classpai.common.PageResult;
import org.example.classpai.common.Result;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.dto.*;
import org.example.classpai.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.classpai.mapper.*;
import org.example.classpai.service.HomeworkService;
import org.example.classpai.vo.HomeworkListVO;
import org.example.classpai.vo.StudentHomeworkVO;
import org.example.classpai.utils.FileTextExtractor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class HomeworkServiceImpl extends BaseCourseServiceImpl implements HomeworkService {

    private static final Logger log = LoggerFactory.getLogger(HomeworkServiceImpl.class);
    private static final String UPLOAD_BASE = System.getProperty("user.dir") + File.separator + "uploads";

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;

    private final HomeworkMapper homeworkMapper;
    private final SubmissionMapper submissionMapper;
    private final HomeworkFileMapper homeworkFileMapper;
    private final SubmitFileMapper submitFileMapper;
    private final RestTemplate restTemplate;
    private ExecutorService sharedExecutor;

    public HomeworkServiceImpl(HomeworkMapper homeworkMapper,
            SubmissionMapper submissionMapper,
            HomeworkFileMapper homeworkFileMapper,
            SubmitFileMapper submitFileMapper,
            RestTemplate restTemplate) {
        this.homeworkMapper = homeworkMapper;
        this.submissionMapper = submissionMapper;
        this.homeworkFileMapper = homeworkFileMapper;
        this.submitFileMapper = submitFileMapper;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    private void initExecutor() {
        sharedExecutor = Executors.newFixedThreadPool(8);
    }

    @PreDestroy
    private void shutdownExecutor() {
        if (sharedExecutor != null) {
            sharedExecutor.shutdown();
        }
    }

    /** 检查课程是否已被当前用户归档，已归档则禁止写操作 */
    private void checkNotArchived(Long courseId, Long userId) {
        UserCourse uc = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getCourseId, courseId)
                        .eq(UserCourse::getUserId, userId));
        if (uc != null && Boolean.TRUE.equals(uc.getArchived())) {
            throw new BusinessException(403, "课程已归档，无法进行此操作");
        }
    }

    @FunctionalInterface
    private interface FileEntitySaver {
        void save(String newName, String filePath, long fileSize, String fileType);
    }

    /**
     * 保存上传文件到磁盘并回调创建对应实体记录
     * 
     * @param files  上传文件数组
     * @param subDir 子目录名，如 "homework"、"submit"
     * @param saver  实体创建回调（接收 newName, filePath, fileSize, fileType）
     */
    private void saveFiles(MultipartFile[] files, String subDir, FileEntitySaver saver) {
        if (files == null || files.length == 0)
            return;
        for (MultipartFile file : files) {
            if (file.isEmpty())
                continue;
            try {
                String dirPath = UPLOAD_BASE + File.separator + subDir;
                File dir = new File(dirPath);
                if (!dir.exists())
                    dir.mkdirs();
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
    @Transactional
    public Result<Homework> createHomework(Long courseId, HomeworkDTO dto, MultipartFile[] files, User user) {
        checkTeacher(courseId, user.getUserId());
        checkNotArchived(courseId, user.getUserId());
        Homework hw = new Homework();
        hw.setCourseId(courseId);
        hw.setTitle(dto.getTitle());
        hw.setContent(dto.getContent());
        hw.setMaxSubmissions(dto.getMaxSubmissions());
        hw.setTotalScore(dto.getTotalScore() != null ? dto.getTotalScore() : 100);
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
        w.eq(Homework::getCourseId, courseId);
        // 先查角色，学生只看已开始的作业
        UserCourse uc = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getCourseId, courseId)
                        .eq(UserCourse::getUserId, user.getUserId()));
        if (uc == null) {
            throw new BusinessException(403, "非课程成员");
        }
        boolean isTeacher = "teacher".equalsIgnoreCase(uc.getRole());
        if (!isTeacher) {
            // 学生只看已开始的作业（未设置开始时间视为已开始）
            w.and(w2 -> w2.isNull(Homework::getStartTime)
                    .or().le(Homework::getStartTime, LocalDateTime.now()));
        }
        w.orderByDesc(Homework::getCreateTime);
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
    @Transactional
    public Result<Submission> submit(Long hwId, String content, MultipartFile[] files, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null)
            throw new BusinessException(404, "作业不存在");
        checkNotArchived(hw.getCourseId(), user.getUserId());
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
        if (hw == null)
            throw new BusinessException(404, "作业不存在");
        checkTeacher(hw.getCourseId(), user.getUserId());
        LambdaQueryWrapper<Submission> w = new LambdaQueryWrapper<>();
        w.eq(Submission::getHwId, hwId).orderByAsc(Submission::getSubmitTime);
        Page<Submission> p = submissionMapper.selectPage(new Page<>(page, pageSize), w);
        return PageResult.of(p.getTotal(), p.getRecords(), p.getCurrent(), p.getSize());
    }

    @Override
    @Transactional
    public Result<?> grade(Long submitId, GradeDTO dto, User user) {
        Submission sub = submissionMapper.selectById(submitId);
        if (sub == null)
            throw new BusinessException(404, "提交记录不存在");
        Homework hw = homeworkMapper.selectById(sub.getHwId());
        checkTeacher(hw.getCourseId(), user.getUserId());
        checkNotArchived(hw.getCourseId(), user.getUserId());
        sub.setScore(dto.getScore());
        sub.setComment(dto.getComment());
        submissionMapper.updateById(sub);
        return Result.success("评分成功");
    }

    @Override
    public Result<List<Submission>> getGradingList(Long hwId, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null)
            throw new BusinessException(404, "作业不存在");
        checkTeacher(hw.getCourseId(), user.getUserId());
        List<Submission> list = submissionMapper.findGradingListByHwId(hwId);
        int maxScore = hw.getTotalScore() != null ? hw.getTotalScore() : 100;
        // 收集有提交记录的 submitId
        List<Long> submitIds = list.stream()
                .filter(s -> s.getSubmitId() != null)
                .map(Submission::getSubmitId)
                .collect(Collectors.toList());
        // 批量查文件
        List<SubmitFile> allFiles = submitIds.isEmpty()
                ? List.of()
                : submitFileMapper.selectList(
                        new LambdaQueryWrapper<SubmitFile>().in(SubmitFile::getSubmitId, submitIds));
        Map<Long, List<SubmitFile>> fileMap = allFiles.stream()
                .collect(Collectors.groupingBy(SubmitFile::getSubmitId));
        for (Submission s : list) {
            if (s.getTotalScore() == null)
                s.setTotalScore(maxScore);
            s.setFiles(fileMap.getOrDefault(s.getSubmitId(), List.of()));
        }
        return Result.success(list);
    }

    @Override
    public Result<Homework> getHomework(Long hwId, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null)
            throw new BusinessException(404, "作业不存在");
        Long count = userCourseMapper.selectCount(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getCourseId, hw.getCourseId())
                        .eq(UserCourse::getUserId, user.getUserId()));
        if (count == 0)
            throw new BusinessException(403, "非课程成员");
        return Result.success(hw);
    }


    @Override
    @Transactional
    public Result<?> gradeByAIBatch(Long hwId, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null)
            throw new BusinessException(404, "作业不存在");
        checkTeacher(hw.getCourseId(), user.getUserId());
        checkNotArchived(hw.getCourseId(), user.getUserId());
        // 获取所有已提交但未批改的提交
        List<Submission> ungraded = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getHwId, hwId)
                        .isNotNull(Submission::getSubmitId)
                        .isNull(Submission::getScore)
                        .orderByDesc(Submission::getSubmitId));
        // 只保留每个学生最新的一份提交
        ungraded = ungraded.stream()
                .collect(Collectors.toMap(
                        Submission::getStudentId,
                        s -> s,
                        (existing, replacement) -> existing))
                .values().stream()
                .collect(Collectors.toList());
        if (ungraded.isEmpty())
            return Result.success("没有待批改的提交");

        // 预扫描：检查所有附件（教师的 + 学生提交的），含有不支持的文件类型则拒绝批改
        List<String> unsupportedAll = new ArrayList<>();

        // 检查教师作业附件
        List<HomeworkFile> hwFiles = homeworkFileMapper.selectList(
                new LambdaQueryWrapper<HomeworkFile>().eq(HomeworkFile::getHwId, hwId));
        for (HomeworkFile hf : hwFiles) {
            if (!FileTextExtractor.isSupported(hf.getFilePath())) {
                unsupportedAll.add(hf.getFileName() != null ? hf.getFileName() : hf.getFilePath());
            }
        }

        // 检查学生提交附件
        for (Submission sub : ungraded) {
            List<SubmitFile> files = submitFileMapper.selectList(
                    new LambdaQueryWrapper<SubmitFile>().eq(SubmitFile::getSubmitId, sub.getSubmitId()));
            for (SubmitFile sf : files) {
                if (!FileTextExtractor.isSupported(sf.getFilePath())) {
                    String name = sf.getFilePath();
                    unsupportedAll.add(name.substring(name.lastIndexOf('/') + 1));
                }
            }
        }

        if (!unsupportedAll.isEmpty()) {
            List<String> unique = unsupportedAll.stream().distinct().collect(Collectors.toList());
            throw new BusinessException(400,
                    "无法使用AI批改。以下文件类型不支持文字提取：" + String.join("、", unique)
                    + "。请确保题目与学生提交的附件为PDF/Word/纯文本格式。");
        }

        String question = buildQuestionText(hw);

        AtomicInteger graded = new AtomicInteger(0);
        Semaphore aiSemaphore = new Semaphore(5); // 最多5个并发AI调用

        List<CompletableFuture<Void>> futures = ungraded.stream()
                .map(sub -> CompletableFuture.runAsync(() -> {
                    try {
                        // ① 提取文档文字（CPU密集，多线程并行）
                        String answer = buildAnswerText(sub);
                        System.out.println(answer);
                        // ② 调用AI评分（IO密集，信号量限制并发数）
                        aiSemaphore.acquire();
                        try {
                            JSONObject body = new JSONObject();
                            body.put("model", "deepseek-chat");
                            body.put("temperature", 0);
                            body.put("stream", false);
                            body.put("messages", buildHomeworkPrompt(question, answer, hw.getTotalScore()));
                            JSONObject response = sendToAI(body);
                            JSONObject choice = response.getJSONArray("choices").getJSONObject(0);
                            String content = choice.getJSONObject("message").getString("content");
                            JSONObject result = JSONObject.parseObject(content);
                            sub.setScore(result.getInteger("score"));
                            sub.setComment(result.getString("comment"));
                            graded.incrementAndGet();
                        } finally {
                            aiSemaphore.release();
                        }
                    } catch (Exception e) {
                        log.error("AI批改失败 submitId={}", sub.getSubmitId(), e);
                    }
                }, sharedExecutor))
                .collect(Collectors.toList());

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 批量更新已评分的提交
        List<Submission> gradedList = ungraded.stream()
                .filter(s -> s.getScore() != null)
                .collect(Collectors.toList());
        if (!gradedList.isEmpty()) {
            submissionMapper.updateById(gradedList);
        }

        return Result.success("批改完成：" + graded.get() + "/" + ungraded.size());
    }

    @Override
    @Transactional
    public Result<?> updateHomeworkTime(Long hwId, Long startTime, Long deadline, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) throw new BusinessException(404, "作业不存在");
        checkTeacher(hw.getCourseId(), user.getUserId());
        // 开始时间：仅未开始时可以修改
        if (startTime != null) {
            if (hw.getStartTime() != null && !LocalDateTime.now().isBefore(hw.getStartTime())) {
                throw new BusinessException(400, "作业已开始，无法修改开始时间");
            }
            hw.setStartTime(LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(startTime), ZoneId.systemDefault()));
        }
        // 截止时间：随时可改
        if (deadline != null) {
            hw.setDeadline(LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(deadline), ZoneId.systemDefault()));
        }
        homeworkMapper.updateById(hw);
        return Result.success("时间修改成功");
    }

    // ========== 以下为私有/辅助方法 ==========
    /** 拼接作业题目文字：正文 + PDF/Word附件提取的文字 */
    private String buildQuestionText(Homework hw) {
        StringBuilder sb = new StringBuilder();
        if (hw.getContent() != null && !hw.getContent().isBlank()) {
            sb.append(hw.getContent()).append("\n");
        }
        // 提取教师上传的PDF/Word附件文字
        List<HomeworkFile> hwFiles = homeworkFileMapper.selectList(
                new LambdaQueryWrapper<HomeworkFile>().eq(HomeworkFile::getHwId, hw.getHwId()));
        List<String> unsupportedNames = new ArrayList<>();
        for (HomeworkFile hf : hwFiles) {
            String fileName = hf.getFileName();
            if (!FileTextExtractor.isSupported(hf.getFilePath())) {
                unsupportedNames.add(fileName);
                continue;
            }
            String text = extractFileText(hf.getFilePath());
            if (!text.isEmpty()) {
                sb.append("\n【附件文字：").append(fileName).append("】\n");
                sb.append(text).append("\n");
            }
        }
        if (!unsupportedNames.isEmpty()) {
            sb.insert(0, "【注意】以下附件文件类型不支持文字提取，AI批改时将忽略其内容："
                    + String.join("、", unsupportedNames) + "\n\n");
        }
        return sb.toString().trim();
    }

    /** 拼接学生答案文字：提交正文 + PDF/Word附件提取的文字 */
    private String buildAnswerText(Submission sub) {
        StringBuilder sb = new StringBuilder();
        if (sub.getSubmitContent() != null && !sub.getSubmitContent().isBlank()) {
            sb.append(sub.getSubmitContent()).append("\n");
        }
        // 提取学生提交的PDF/Word附件文字
        List<SubmitFile> files = submitFileMapper.selectList(
                new LambdaQueryWrapper<SubmitFile>().eq(SubmitFile::getSubmitId, sub.getSubmitId()));
        List<String> unsupportedNames = new ArrayList<>();
        for (SubmitFile sf : files) {
            String fileName = sf.getFilePath();
            String shortName = fileName.substring(fileName.lastIndexOf('/') + 1);
            if (!FileTextExtractor.isSupported(fileName)) {
                unsupportedNames.add(shortName);
                continue;
            }
            String text = extractFileText(fileName);
            if (!text.isEmpty()) {
                sb.append("\n【附件文字】\n").append(text).append("\n");
            }
        }
        if (!unsupportedNames.isEmpty()) {
            sb.insert(0, "【注意】以下附件文件类型不支持文字提取，AI将无法读取其内容："
                    + String.join("、", unsupportedNames) + "。请仅基于可用文本内容评分。\n\n");
        }
        return sb.toString().trim();
    }

    private String extractFileText(String filePath) {
        if (filePath == null) return "";
        // 文件路径格式：/uploads/xxx/yyy.pdf
        File file = new File(System.getProperty("user.dir") + File.separator + "uploads",
                filePath.replace("/uploads/", "").replace("/", File.separator));
        if (!file.exists()) return "";
        try {
            String text = FileTextExtractor.extract(file);
            return text;
        } catch (Exception e) {
            return "";
        }
    }

    private List<Map<String, String>> buildHomeworkPrompt(String question, String answer, Integer totalScore) {
        String systemPrompt = "你是严谨的作业批改老师，请严格按照以下评分标准批改学生作业：\n" +
                "\n" +
                "【评分规则 - 请逐项评估再计算总分】\n" +
                "1. 答案正确性（占40%）：答案与标准答案或题目要求是否一致，关键结论、数据、公式是否正确\n" +
                "2. 知识点准确性（占25%）：涉及的概念、定理、术语是否使用正确，有无原理性错误\n" +
                "3. 内容完整性（占20%）：是否回答了题目的所有要点，有无遗漏重要内容\n" +
                "4. 逻辑与表达（占15%）：推理过程是否清晰，表达是否通顺，有无自相矛盾\n" +
                "\n" +
                "【扣分标准】\n" +
                "- 完全正确、无错误 → 满分\n" +
                "- 核心结论错误 → 扣除40%-60%分数\n" +
                "- 缺少关键要点 → 每个要点扣10%-20%\n" +
                "- 概念或术语错误 → 每处扣5%-10%\n" +
                "- 逻辑混乱或答非所问 → 扣除20%-40%\n" +
                "- 空白或完全无关 → 0分\n" +
                "\n" +
                "只输出JSON格式，不要任何额外文字：\n" +
                "{\"score\": 实际得分, \"comment\": \"简要说明扣分原因和主要问题\"}";
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

    private String calcSubmitStatus(Homework hw, List<Submission> submissions) {
        if (!submissions.isEmpty() && submissions.get(0).getScore() != null)
            return "graded";
        if (!submissions.isEmpty())
            return "submitted";
        if (hw.getDeadline() != null && LocalDateTime.now().isAfter(hw.getDeadline()))
            return "overdue";
        return "unsubmitted";
    }

    @Override
    public Result<StudentHomeworkVO> getStudentHomeworkDetail(Long hwId, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) throw new BusinessException(404, "作业不存在");
        checkMembership(hw.getCourseId(), user.getUserId());
        StudentHomeworkVO vo = buildBaseVO(hw);
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
    public Result<StudentHomeworkVO> getTeacherHomeworkDetail(Long hwId, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) throw new BusinessException(404, "作业不存在");
        checkTeacher(hw.getCourseId(), user.getUserId());
        return Result.success(buildBaseVO(hw));
    }

    @Override
    public Result<StudentHomeworkVO> getSubmitPageData(Long hwId, User user) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) throw new BusinessException(404, "作业不存在");
        checkMembership(hw.getCourseId(), user.getUserId());
        StudentHomeworkVO vo = buildBaseVO(hw);
        vo.setSubmissions(submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getHwId, hwId)
                        .eq(Submission::getStudentId, user.getUserId())
                        .orderByDesc(Submission::getSubmitTime)));
        vo.setSubmitStatus(calcSubmitStatus(hw, vo.getSubmissions()));
        vo.setSubmittedCount(vo.getSubmissions().size());
        return Result.success(vo);
    }

    /** 构建作业基础 VO，包含课程名、附件等公共字段 */
    private StudentHomeworkVO buildBaseVO(Homework hw) {
        Course course = courseMapper.selectById(hw.getCourseId());
        StudentHomeworkVO vo = new StudentHomeworkVO();
        BeanUtils.copyProperties(hw, vo);
        vo.setCourseName(course != null ? course.getCourseName() : "");
        vo.setTotalScore(hw.getTotalScore() != null ? hw.getTotalScore() : 100);
        vo.setFiles(homeworkFileMapper.selectList(
                new LambdaQueryWrapper<HomeworkFile>().eq(HomeworkFile::getHwId, hw.getHwId())));
        for (HomeworkFile f : vo.getFiles()) {
            String name = f.getFilePath() != null
                    ? f.getFilePath().substring(f.getFilePath().lastIndexOf("/") + 1) : "";
            f.setOriginalName(name);
            f.setFileName(name);
        }
        return vo;
    }
}
