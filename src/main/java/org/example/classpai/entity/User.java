package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    /** TEACHER / STUDENT */
    private String role;

    private String name;

    private String email;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
