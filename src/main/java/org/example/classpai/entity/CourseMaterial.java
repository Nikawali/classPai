package org.example.classpai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_material")
public class CourseMaterial {
    @TableId(type = IdType.AUTO)
    private Long materialId;

    private Long courseId;

    /** 父文件夹ID，NULL=根目录 */
    private Long parentId;

    /** 名称 */
    private String name;

    /** 类型: folder / file / link */
    private String type;

    /** 附件存储路径（仅 file 类型） */
    private String filePath;

    /** 文件大小(字节)（仅 file 类型） */
    private Long fileSize;

    /** 外链地址（仅 link 类型） */
    private String url;

    /** 排序 */
    private Integer sortOrder;

    /** 上传者 */
    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
