package org.example.classpai.dto;

import lombok.Data;

@Data
public class HomeworkDTO {
    private String title;
    private String content;
    /** 开始时间（Unix 秒级时间戳） */
    private Long startTime;
    /** 截止时间（Unix 秒级时间戳） */
    private Long deadline;
    private Integer maxSubmissions;
}
