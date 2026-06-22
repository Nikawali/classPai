package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("homework")
public class Homework {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属课程ID */
    private Long courseId;

    private String title;

    private String description;

    /** 截止时间 */
    private LocalDateTime dueDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
