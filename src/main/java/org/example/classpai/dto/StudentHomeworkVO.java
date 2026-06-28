package org.example.classpai.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生端作业详情聚合 VO
 * 聚合作业信息、课程名、附件、历史提交记录
 */
@Data
public class StudentHomeworkVO {
    private Long hwId;
    private Long courseId;
    private String title;
    private String content;
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private String courseName;
    private String submitStatus;
    private Integer latestScore;
    private String latestComment;
    private List<HwFileVO> files;
    private List<SubVO> submissions;

    @Data
    public static class HwFileVO {
        private Long fileId;
        private String originalName;
        private Long fileSize;
        private String filePath;
    }

    @Data
    public static class SubVO {
        private Long submitId;
        private Long studentId;
        private String studentName;
        private String submitContent;
        private LocalDateTime submitTime;
        private Integer score;
        private String comment;
    }
}
