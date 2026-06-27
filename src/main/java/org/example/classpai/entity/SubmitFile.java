package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("submit_file")
public class SubmitFile {
    @TableId(type = IdType.AUTO)
    private Long sFileId;

    private Long submitId;

    private String filePath;

    private Long fileSize;

    private String fileType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;
}
