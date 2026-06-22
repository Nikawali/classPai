package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_course")
public class UserCourse {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生ID */
    private Long userId;

    /** 课程ID */
    private Long courseId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime enrollTime;
}
