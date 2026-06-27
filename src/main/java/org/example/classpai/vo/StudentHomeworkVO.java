package org.example.classpai.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.classpai.entity.Homework;
import org.example.classpai.entity.HomeworkFile;
import org.example.classpai.entity.Submission;

import java.util.List;

/**
 * 学生作业视图 VO（作业详情页 + 提交页共用）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StudentHomeworkVO extends Homework {
    @TableField(exist = false)
    private String courseName;
    @TableField(exist = false)
    private List<HomeworkFile> files;
    @TableField(exist = false)
    private List<Submission> submissions;
    @TableField(exist = false)
    private String submitStatus;
    @TableField(exist = false)
    private Double latestScore;
    @TableField(exist = false)
    private Integer submittedCount;
}
