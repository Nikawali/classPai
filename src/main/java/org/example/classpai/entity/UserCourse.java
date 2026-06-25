package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_course")
public class UserCourse {
    @TableId(value = "id", type = IdType.AUTO)
    private Long csId;

    private Long userId;

    private Long courseId;

    /** 在该课程中的角色：teacher / student */
    private String role;

    @TableField(exist = false)
    private BigDecimal score;

    /** 是否置顶 */
    private Boolean pinned;

    /** 置顶排序（越小越靠前） */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinTime;
}
