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

    Result<Homework> createHomework(Long courseId, HomeworkDTO dto, MultipartFile[] files, User user);

    PageResult<Homework> listHomework(Long courseId, User user, int page, int pageSize);

    Result<Submission> submit(Long homeworkId, SubmissionDTO dto, User user);

    PageResult<Submission> listSubmissions(Long homeworkId, User user, int page, int pageSize);

    Result<?> grade(Long submissionId, GradeDTO dto, User user);

    Result<List<Submission>> getGradingList(Long hwId, User user);

    Result<Homework> getHomework(Long hwId, User user);

    Result<?> gradeByAI(Long submitId, User user);

    Result<?> getStudentHomeworkDetail(Long hwId, User user);

    Result<?> getSubmitPageData(Long hwId, User user);
}
