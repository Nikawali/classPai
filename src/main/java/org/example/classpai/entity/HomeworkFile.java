package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("homework_file")
public class HomeworkFile {
    @TableId(type = IdType.AUTO)
    private Long fileId;

    private Long hwId;

    private String filePath;

    private Long fileSize;

    private String fileType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;

    /** 瞬态：文件名（从 filePath 提取） */
    @TableField(exist = false)
    private String originalName;

    /** 瞬态：文件名（同 originalName，兼容不同前端） */
    @TableField(exist = false)
    private String fileName;
}
