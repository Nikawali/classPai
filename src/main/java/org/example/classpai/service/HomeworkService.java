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

    Result<?> gradeByAI(Long submitId, User user);

    /** 学生端：获取作业详情（含提交记录、文件、状态） */
    Result<StudentHomeworkVO> getStudentHomeworkDetail(Long hwId, User user);

    /** 学生端：获取提交页数据 */
    Result<StudentHomeworkVO> getSubmitPageData(Long hwId, User user);
}
