package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teacher_course")
public class TeacherCourse {
    @TableId(type = IdType.AUTO)
    private Long tcId;

    /** FK → user.user_id */
    private Long teacherUserId;

    /** FK → course.course_id */
    private Long courseId;

    private Integer roleInCourse;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime bindTime;
}
