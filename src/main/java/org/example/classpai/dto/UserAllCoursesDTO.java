package org.example.classpai.dto;

import lombok.Data;
import org.example.classpai.entity.Course;

import java.util.List;

@Data
public class UserAllCoursesDTO {
    /** 置顶课程列表（按 sortOrder 排序） */
    private List<Course> pinnedCourses;
    /** 所有课程按学年学期分组 */
    private List<CourseGroupDTO> groups;
}
