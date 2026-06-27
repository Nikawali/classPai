package org.example.classpai.service;

import org.example.classpai.common.PageResult;
import org.example.classpai.common.Result;
import org.example.classpai.dto.GradeDTO;
import org.example.classpai.dto.HomeworkDTO;
import org.example.classpai.dto.SubmissionDTO;
import org.example.classpai.entity.Homework;
import org.example.classpai.entity.Submission;
import org.example.classpai.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HomeworkService {

    /** 教师布置作业（含文件上传） */
    Result<Homework> createHomework(Long courseId, HomeworkDTO dto, MultipartFile[] files, User user);

    /** 查看某课程作业列表 */
    PageResult<Homework> listHomework(Long courseId, User user, int page, int pageSize);

    /** 学生提交作业 */
    Result<Submission> submit(Long homeworkId, SubmissionDTO dto, User user);

    /** 教师查看某作业所有提交 */
    PageResult<Submission> listSubmissions(Long homeworkId, User user, int page, int pageSize);

    /** 教师批改评分 */
    Result<?> grade(Long submissionId, GradeDTO dto, User user);

    /** 教师批阅页：获取作业所有学生的提交状态 */
    Result<List<Submission>> getGradingList(Long hwId, User user);

    /** 获取单个作业信息 */
    Result<Homework> getHomework(Long hwId, User user);

    Result<?> gradeByAI(Long submitId,  User user);
}
