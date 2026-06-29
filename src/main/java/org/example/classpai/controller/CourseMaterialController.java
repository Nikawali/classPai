package org.example.classpai.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.classpai.common.Result;
import org.example.classpai.entity.User;
import org.example.classpai.service.CourseMaterialService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/material")
public class CourseMaterialController {

    private final CourseMaterialService materialService;

    public CourseMaterialController(CourseMaterialService materialService) {
        this.materialService = materialService;
    }

    /** 列出资料 */
    @GetMapping("/course/{courseId}")
    public Result<?> list(@PathVariable Long courseId,
                          @RequestParam(required = false) Long parentId,
                          HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return materialService.listMaterials(courseId, parentId, user);
    }

    /** 创建文件夹 */
    @PostMapping("/course/{courseId}/folder")
    public Result<?> createFolder(@PathVariable Long courseId,
                                  @RequestParam(required = false) Long parentId,
                                  @RequestParam String name,
                                  HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return materialService.createFolder(courseId, parentId, name, user);
    }

    /** 上传附件 */
    @PostMapping("/course/{courseId}/file")
    public Result<?> uploadFiles(@PathVariable Long courseId,
                                 @RequestParam(required = false) Long parentId,
                                 @RequestParam("files") MultipartFile[] files,
                                 HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return materialService.uploadFiles(courseId, parentId, files, user);
    }

    /** 添加外链 */
    @PostMapping("/course/{courseId}/link")
    public Result<?> addLink(@PathVariable Long courseId,
                             @RequestParam(required = false) Long parentId,
                             @RequestParam String name,
                             @RequestParam String url,
                             HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return materialService.addLink(courseId, parentId, name, url, user);
    }

    /** 删除资料 */
    @DeleteMapping("/{materialId}")
    public Result<?> delete(@PathVariable Long materialId,
                            HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return materialService.deleteMaterial(materialId, user);
    }

    /** 移动资料 */
    @PutMapping("/{materialId}/move")
    public Result<?> move(@PathVariable Long materialId,
                          @RequestParam(required = false) Long targetParentId,
                          HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return materialService.moveMaterial(materialId, targetParentId, user);
    }

    /** 重命名 */
    @PutMapping("/{materialId}/rename")
    public Result<?> rename(@PathVariable Long materialId,
                            @RequestParam String name,
                            HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return materialService.renameMaterial(materialId, name, user);
    }
}
