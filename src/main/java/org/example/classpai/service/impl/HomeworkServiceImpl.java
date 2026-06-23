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

@Service
public class HomeworkServiceImpl implements HomeworkService {

    private final HomeworkMapper homeworkMapper;
    private final SubmissionMapper submissionMapper;
    private final CourseMapper courseMapper;
    private final TeacherCourseMapper teacherCourseMapper;

    public HomeworkServiceImpl(HomeworkMapper homeworkMapper,
                               SubmissionMapper submissionMapper,
                               CourseMapper courseMapper,
                               TeacherCourseMapper teacherCourseMapper) {
        this.homeworkMapper = homeworkMapper;
        this.submissionMapper = submissionMapper;
        this.courseMapper = courseMapper;
        this.teacherCourseMapper = teacherCourseMapper;
    }

    /** 校验用户是否该课程教师 */
    private void checkTeacher(Long courseId, Long userId) {
        LambdaQueryWrapper<TeacherCourse> w = new LambdaQueryWrapper<>();
        w.eq(TeacherCourse::getCourseId, courseId)
         .eq(TeacherCourse::getTeacherUserId, userId);
        if (teacherCourseMapper.selectCount(w) == 0) {
            throw new BusinessException(403, "无权操作");
        }
    }

    @Override
    public Result<Homework> createHomework(Long courseId, HomeworkDTO dto, User user) {
        checkTeacher(courseId, user.getUserId());

        Homework hw = new Homework();
        hw.setCourseId(courseId);
        hw.setTitle(dto.getTitle());
        hw.setContent(dto.getContent());
        hw.setDeadline(dto.getDeadline());
        homeworkMapper.insert(hw);
        return Result.success(hw);
    }

    @Override
    public PageResult<Homework> listHomework(Long courseId, int page, int pageSize) {
        LambdaQueryWrapper<Homework> w = new LambdaQueryWrapper<>();
        w.eq(Homework::getCourseId, courseId)
         .orderByDesc(Homework::getCreateTime);
        Page<Homework> p = homeworkMapper.selectPage(new Page<>(page, pageSize), w);
        return PageResult.of(p.getTotal(), p.getRecords(), p.getCurrent(), p.getSize());
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
}
