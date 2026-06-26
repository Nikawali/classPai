package org.example.classpai.service.impl;

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
import org.springframework.stereotype.Service;
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

    private final HomeworkMapper homeworkMapper;
    private final SubmissionMapper submissionMapper;
    private final UserCourseMapper userCourseMapper;
    private final HomeworkFileMapper homeworkFileMapper;

    public HomeworkServiceImpl(HomeworkMapper homeworkMapper,
                               SubmissionMapper submissionMapper,
                               UserCourseMapper userCourseMapper,
                               HomeworkFileMapper homeworkFileMapper) {
        this.homeworkMapper = homeworkMapper;
        this.submissionMapper = submissionMapper;
        this.userCourseMapper = userCourseMapper;
        this.homeworkFileMapper = homeworkFileMapper;
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
                    String newName = UUID.randomUUID().toString() + ext;
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

        // 查用户在当前课程中的角色（不是全局角色）
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

    @Override
    public Result<?> grade(Long submitId, GradeDTO dto, User user) {
        Submission sub = submissionMapper.selectById(submitId);
        if (sub == null) throw new BusinessException(404, "提交记录不存在");

        Homework hw = homeworkMapper.selectById(sub.getHwId());
        checkTeacher(hw.getCourseId(), user.getUserId());

        sub.setScore(dto.getScore());
        sub.setComment(dto.getFeedback());
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
}
