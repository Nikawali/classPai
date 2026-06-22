package org.example.classpai.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomeworkDTO {
    private String title;
    private String description;
    private LocalDateTime dueDate;
}
