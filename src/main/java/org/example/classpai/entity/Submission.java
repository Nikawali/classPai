package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("submission")
public class Submission {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作业ID */
    private Long homeworkId;

    /** 提交学生ID */
    private Long studentId;

    /** 作业内容 */
    private String content;

    /** 附件URL */
    private String fileUrl;

    /** 教师评分 */
    private Integer score;

    /** 教师评语 */
    private String feedback;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime submitTime;
}
