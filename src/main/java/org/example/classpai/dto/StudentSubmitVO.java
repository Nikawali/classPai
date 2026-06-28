package org.example.classpai.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生端作业提交页聚合 VO
 */
@Data
public class StudentSubmitVO {
    // ===== 作业基础信息 =====
    private Long hwId;
    private Long courseId;
    private String title;
    private String content;
    private String courseName;
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private Integer totalScore;
    private Integer submitLimit;
    private String fileFormats;
    private Boolean allowLate;

    // ===== 学生提交状态 =====
    private String submitStatus;
    private Integer submittedCount;
    private Integer remainingCount;

    // ===== 教师附件 =====
    private List<HwFileVO> teacherFiles;

    // ===== 历史提交记录 =====
    private List<SubmissionVO> submissions;

    @Data
    public static class HwFileVO {
        private Long fileId;
        private String fileName;
        private Long fileSize;
        private String filePath;
    }

    @Data
    public static class SubmissionVO {
        private Long submitId;
        private LocalDateTime submitTime;
        private String submitContent;
        private Integer score;
        private String comment;
        private List<SubFileVO> files;
    }

    @Data
    public static class SubFileVO {
        private Long sFileId;
        private String fileName;
        private Long fileSize;
        private String filePath;
    }
}
