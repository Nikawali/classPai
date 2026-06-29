package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long courseId;

    private String courseName;

    private String courseCode;

    private String courseIntro;

    private LocalDate startDate;

    private LocalDate endDate;

    private String semester;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 学生人数（非数据库字段，查询时临时填充） */
    @TableField(exist = false)
    private Integer studentCount;

    /** 当前用户在该课程中的角色（非数据库字段，查询时临时填充） */
    @TableField(exist = false)
    private String userRole;

    /** 当前用户是否置顶该课程 */
    @TableField(exist = false)
    private Boolean pinned;

    /** 当前用户的置顶排序 */
    @TableField(exist = false)
    private Integer sortOrder;

    /** 当前用户是否已归档该课程 */
    @TableField(exist = false)
    private Boolean archived;

    /** 是否禁止学生讨论 */
    private Boolean discussionLocked;
}
