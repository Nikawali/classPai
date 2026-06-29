package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reply")
public class Reply {
    @TableId(type = IdType.AUTO)
    private Long replyId;

    private Long topicId;

    /** NULL 表示匿名 */
    private Long userId;

    private String content;

    private Boolean isAnonymous;

    private LocalDateTime createTime;

    /** 回复人姓名（非数据库字段） */
    @TableField(exist = false)
    private String userName;
}
