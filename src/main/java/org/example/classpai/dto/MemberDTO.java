package org.example.classpai.dto;

import lombok.Data;

/** 课程成员视图（教师/学生通用） */
@Data
public class MemberDTO {
    private Long userId;
    private String userName;
    /**
     * 在该课程中的角色：teacher / student
     */
    private String role;
}
