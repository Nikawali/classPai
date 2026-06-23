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

    /** TEACHER / STUDENT */
    private String role;

    private String phone;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
