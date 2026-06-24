package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_course")
public class UserCourse {
    @TableId(type = IdType.AUTO)
    private Long csId;

    private Long userId;

    private Long courseId;

    /** 在该课程中的角色：teacher / student */
    private String role;

    private BigDecimal score;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime selectTime;
}
