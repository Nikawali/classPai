package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long courseId;

    private String courseName;

    /** 选课码，学生通过此码加入，需手动 ALTER TABLE 添加此列 */
    private String courseCode;

    private String courseIntro;

    private LocalDate startDate;

    private LocalDate endDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
