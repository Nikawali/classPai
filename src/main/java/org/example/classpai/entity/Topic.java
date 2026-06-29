package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("topic")
public class Topic {
    @TableId(type = IdType.AUTO)
    private Long topicId;

    private Long courseId;

    private Long userId;

    private String title;

    private String content;

    private Boolean isPinned;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 发帖人姓名（非数据库字段） */
    @TableField(exist = false)
    private String userName;

    /** 回复数（非数据库字段） */
    @TableField(exist = false)
    private Integer replyCount;
}
