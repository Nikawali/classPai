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
    private Integer maxSubmissions;

    // ========== 瞬态字段（不存库，listHomework 时填充） ==========
    /** 学生视角：当前学生是否已提交 */
    @TableField(exist = false)
    private Boolean submitted;
    /** 教师视角：已提交人数 */
    @TableField(exist = false)
    private Integer submitCount;
    /** 教师视角：已批改人数 */
    @TableField(exist = false)
    private Integer gradedCount;
    /** 教师视角：课程总学生人数 */
    @TableField(exist = false)
    private Integer totalStudents;
    /** 作业满分 */
    @TableField(exist = false)
    private Integer totalScore;
}
