package org.example.classpai.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    /** TEACHER / STUDENT */
    private String role;
    private String name;
    private String email;
}
