package org.example.classpai.controller;

import jakarta.servlet.http.HttpSession;
import org.example.classpai.common.Result;
import org.example.classpai.dto.LoginDTO;
import org.example.classpai.dto.RegisterDTO;
import org.example.classpai.entity.User;
import org.example.classpai.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto) {
        return userService.register(dto);
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody LoginDTO dto, HttpSession session) {
        return userService.login(dto, session);
    }

    @GetMapping("/current")
    public Result<User> current(HttpSession session) {
        return userService.getCurrentUser(session);
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpSession session) {
        session.invalidate();
        return Result.success("已退出");
    }
}
