package org.example.classpai.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.classpai.auth.service.TokenService;
import org.example.classpai.common.Result;
import org.example.classpai.dto.RegisterDTO;
import org.example.classpai.dto.UpdateProfileDTO;
import org.example.classpai.entity.User;
import org.example.classpai.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @GetMapping("/send-code")
    public Result<?> sendCode(@RequestParam String phone) {
        return userService.sendCode(phone);
    }

    @GetMapping("/send-code-profile")
    public Result<?> sendCodeForProfile(@RequestParam String phone,
            HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return userService.sendCodeForProfile(phone, currentUser.getUserId());
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto) {
        return userService.register(dto);
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody UpdateProfileDTO dto,
            HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return userService.updateProfile(dto, currentUser.getUserId());
    }

    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader("Authorization") String auth) {
        if (auth != null && auth.startsWith("Bearer ")) {
            tokenService.revokeToken(auth.substring(7));
        }
        return Result.success("已退出");
    }
}
