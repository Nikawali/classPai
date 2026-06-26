package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("homework_submit")
public class Submission {
    @TableId(type = IdType.AUTO)
    private Long submitId;

    private Long hwId;

    private Long studentId;

    private String submitContent;

    private Integer score;

    private String comment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime submitTime;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private Integer totalScore;

    @TableField(exist = false)
    private Boolean submitted;
}
