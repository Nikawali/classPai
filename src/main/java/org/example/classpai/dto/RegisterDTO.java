package org.example.classpai.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    /** 学号/工号（纯数字） */
    private Long userId;
    /** 真实姓名 */
    private String userName;
    private String phone;
    private String password;
    private String gender;
    private String role;        // TEACHER / STUDENT
    private String school;      // 学校（仅学生填）
    private String college;     // 学院（仅学生填）
    private String major;       // 专业（仅学生填）
    private String code;        // 验证码
}
