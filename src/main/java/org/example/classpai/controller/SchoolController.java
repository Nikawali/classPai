package org.example.classpai.controller;

import org.example.classpai.common.Result;
import org.example.classpai.entity.School;
import org.example.classpai.service.SchoolService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/school")
public class SchoolController {

    private final SchoolService schoolService;

    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping("/list")
    public Result<List<School>> list() {
        return Result.success(schoolService.list());
    }
}
