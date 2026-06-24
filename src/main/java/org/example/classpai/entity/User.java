package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    /** 学号/工号，用户指定 */
    @TableId(type = IdType.INPUT)
    private Long userId;

    /** 登录用户名，唯一 */
    private String userName;

    private String password;

    /** teacher / student */
    private String role;

    private String phone;

    private String gender;

    /** 学校 */
    private String school;

    /** 学院 */
    private String college;

    /** 专业 */
    private String major;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
