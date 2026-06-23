package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("student")
public class Student {
    @TableId(type = IdType.INPUT)
    private Long studentId;

    private String gender;

    private String school;

    private String name;

    private String major;

    private String college;
}
