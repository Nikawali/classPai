package org.example.classpai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.classpai.common.PageResult;
import org.example.classpai.common.Result;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.dto.GradeDTO;
import org.example.classpai.dto.HomeworkDTO;
import org.example.classpai.dto.SubmissionDTO;
import org.example.classpai.entity.Course;
import org.example.classpai.entity.Homework;
import org.example.classpai.entity.Submission;
import org.example.classpai.entity.User;
import org.example.classpai.mapper.CourseMapper;
import org.example.classpai.mapper.HomeworkMapper;
import org.example.classpai.mapper.SubmissionMapper;
import org.example.classpai.service.HomeworkService;
import org.springframework.stereotype.Service;

@Service
public class HomeworkServiceImpl implements HomeworkService {

    private final HomeworkMapper homeworkMapper;
    private final SubmissionMapper submissionMapper;
    private final CourseMapper courseMapper;

    public HomeworkServiceImpl(HomeworkMapper homeworkMapper,
                               SubmissionMapper submissionMapper,
                               CourseMapper courseMapper) {
        this.homeworkMapper = homeworkMapper;
        this.submissionMapper = submissionMapper;
        this.courseMapper = courseMapper;
    }

    @Override
    public Result<Homework> createHomework(Long courseId, HomeworkDTO dto, User teacher) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        if (!course.getTeacherId().equals(teacher.getId())) {
            throw new BusinessException(403, "无权操作该课程");
        }

        Homework homework = new Homework();
        homework.setCourseId(courseId);
        homework.setTitle(dto.getTitle());
        homework.setDescription(dto.getDescription());
        homework.setDueDate(dto.getDueDate());
        homeworkMapper.insert(homework);
        return Result.success(homework);
    }

    @Override
    public PageResult<Homework> listHomework(Long courseId, int page, int pageSize) {
        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Homework::getCourseId, courseId)
               .orderByDesc(Homework::getCreateTime);
        Page<Homework> p = homeworkMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return PageResult.of(p.getTotal(), p.getRecords(), p.getCurrent(), p.getSize());
    }

    @Override
    public Result<Submission> submit(Long homeworkId, SubmissionDTO dto, User student) {
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            throw new BusinessException(404, "作业不存在");
        }

        // 查重：同一学生对同一作业只能提交一次（可替换内容）
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Submission::getHomeworkId, homeworkId)
               .eq(Submission::getStudentId, student.getId());
        Submission exist = submissionMapper.selectOne(wrapper);

        if (exist != null) {
            exist.setContent(dto.getContent());
            exist.setFileUrl(dto.getFileUrl());
            submissionMapper.updateById(exist);
            return Result.success(exist);
        }

        Submission submission = new Submission();
        submission.setHomeworkId(homeworkId);
        submission.setStudentId(student.getId());
        submission.setContent(dto.getContent());
        submission.setFileUrl(dto.getFileUrl());
        submissionMapper.insert(submission);
        return Result.success(submission);
    }

    @Override
    public PageResult<Submission> listSubmissions(Long homeworkId, User teacher,
                                                   int page, int pageSize) {
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            throw new BusinessException(404, "作业不存在");
        }
        Course course = courseMapper.selectById(homework.getCourseId());
        if (!course.getTeacherId().equals(teacher.getId())) {
            throw new BusinessException(403, "无权查看");
        }

        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Submission::getHomeworkId, homeworkId)
               .orderByAsc(Submission::getSubmitTime);
        Page<Submission> p = submissionMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return PageResult.of(p.getTotal(), p.getRecords(), p.getCurrent(), p.getSize());
    }

    @Override
    public Result<?> grade(Long submissionId, GradeDTO dto, User teacher) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }

        Homework homework = homeworkMapper.selectById(submission.getHomeworkId());
        Course course = courseMapper.selectById(homework.getCourseId());
        if (!course.getTeacherId().equals(teacher.getId())) {
            throw new BusinessException(403, "无权评分");
        }

        submission.setScore(dto.getScore());
        submission.setFeedback(dto.getFeedback());
        submissionMapper.updateById(submission);
        return Result.success("评分成功");
    }
}
