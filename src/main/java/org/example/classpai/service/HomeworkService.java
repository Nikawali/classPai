package org.example.classpai.service;

import org.example.classpai.common.PageResult;
import org.example.classpai.common.Result;
import org.example.classpai.dto.GradeDTO;
import org.example.classpai.dto.HomeworkDTO;
import org.example.classpai.entity.Homework;
import org.example.classpai.entity.Submission;
import org.example.classpai.entity.User;
import org.example.classpai.vo.HomeworkListVO;
import org.example.classpai.vo.StudentHomeworkVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HomeworkService {

    Result<Homework> createHomework(Long courseId, HomeworkDTO dto, MultipartFile[] files, User user);

    /** 查看某课程作业列表（含教师统计/学生提交状态） */
    PageResult<HomeworkListVO> listHomework(Long courseId, User user, int page, int pageSize);

    Result<Submission> submit(Long homeworkId, String content, MultipartFile[] files, User user);

    PageResult<Submission> listSubmissions(Long homeworkId, User user, int page, int pageSize);

    Result<?> grade(Long submissionId, GradeDTO dto, User user);

    Result<List<Submission>> getGradingList(Long hwId, User user);

    Result<Homework> getHomework(Long hwId, User user);

    /** 教师端：AI 一键批改所有未批改的提交 */
    Result<?> gradeByAIBatch(Long hwId, User user);

    /** 学生端：获取作业详情（含提交记录、文件、状态） */
    Result<StudentHomeworkVO> getStudentHomeworkDetail(Long hwId, User user);

    /** 教师端：获取作业详情（含文件、不含学生提交记录） */
    Result<StudentHomeworkVO> getTeacherHomeworkDetail(Long hwId, User user);

    /** 学生端：获取提交页数据 */
    Result<StudentHomeworkVO> getSubmitPageData(Long hwId, User user);

    /** 教师修改作业时间（开始时间未开始才能改，截止时间随时可改） */
    Result<?> updateHomeworkTime(Long hwId, Long startTime, Long deadline, User user);
}
