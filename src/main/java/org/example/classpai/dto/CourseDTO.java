package org.example.classpai.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseDTO {
    private String courseName;
    private String courseIntro;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
