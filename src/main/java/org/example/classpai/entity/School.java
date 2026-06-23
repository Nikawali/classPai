package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("school")
public class School {
    @TableId(value = "school_id", type = IdType.AUTO)
    private Long id;

    private String schoolName;
}
