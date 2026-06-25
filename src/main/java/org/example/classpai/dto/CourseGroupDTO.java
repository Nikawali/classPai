package org.example.classpai.dto;

import lombok.Data;
import org.example.classpai.entity.Course;

import java.util.List;

@Data
public class CourseGroupDTO {
    /** 分组名称，如 "2025-2026第一学期" */
    private String groupName;
    /** 该分组下的课程列表 */
    private List<Course> courses;
}
