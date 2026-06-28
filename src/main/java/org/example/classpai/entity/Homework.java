package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("homework")
public class Homework {
    @TableId(type = IdType.AUTO)
    private Long hwId;

    private Long courseId;

    private String title;

    private String content;

    private LocalDateTime startTime;

    private LocalDateTime deadline;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 该作业最多允许提交次数（null=不限） */
    @TableField(exist = false)
    private Integer maxSubmissions;

    /** 作业满分 */
    @TableField(exist = false)
    private Integer totalScore;
}
