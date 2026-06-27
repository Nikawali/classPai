package org.example.classpai.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.classpai.entity.Homework;

/**
 * 作业列表视图 VO（教师端统计 + 学生端提交状态）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HomeworkListVO extends Homework {
    @TableField(exist = false)
    private Integer submitCount;
    @TableField(exist = false)
    private Integer gradedCount;
    @TableField(exist = false)
    private Integer totalStudents;
    @TableField(exist = false)
    private Boolean submitted;
}
