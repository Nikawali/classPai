package org.example.classpai.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CourseDTO {
    private String courseName;
    private String courseIntro;
    private LocalDate startDate;
    private LocalDate endDate;
}
