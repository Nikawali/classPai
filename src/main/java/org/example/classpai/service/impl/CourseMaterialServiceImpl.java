package org.example.classpai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.classpai.common.Result;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.entity.CourseMaterial;
import org.example.classpai.entity.User;
import org.example.classpai.mapper.CourseMaterialMapper;
import org.example.classpai.service.CourseMaterialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CourseMaterialServiceImpl extends BaseCourseServiceImpl implements CourseMaterialService {

    private static final String UPLOAD_BASE = System.getProperty("user.dir") + File.separator + "uploads";

    private final CourseMaterialMapper materialMapper;

    public CourseMaterialServiceImpl(CourseMaterialMapper materialMapper) {
        this.materialMapper = materialMapper;
    }

    // ==================== 权限检查 ====================

    private CourseMaterial getOrThrow(Long materialId) {
        CourseMaterial m = materialMapper.selectById(materialId);
        if (m == null) throw new BusinessException(404, "资料不存在");
        return m;
    }

    // ==================== 业务方法 ====================

    @Override
    public Result<?> listMaterials(Long courseId, Long parentId, User user) {
        checkMembership(courseId, user.getUserId());
        List<CourseMaterial> all = materialMapper.selectList(
                new LambdaQueryWrapper<CourseMaterial>()
                        .eq(CourseMaterial::getCourseId, courseId)
                        .eq(parentId != null,
                                CourseMaterial::getParentId, parentId)
                        .isNull(parentId == null,
                                CourseMaterial::getParentId)
                        .orderByAsc(CourseMaterial::getType)
                        .orderByAsc(CourseMaterial::getSortOrder)
                        .orderByAsc(CourseMaterial::getCreateTime));
        return Result.success(all);
    }

    @Override
    @Transactional
    public Result<?> createFolder(Long courseId, Long parentId, String name, User user) {
        checkTeacher(courseId, user.getUserId());
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(400, "文件夹名不能为空");
        }
        // 检查同名
        long count = materialMapper.selectCount(new LambdaQueryWrapper<CourseMaterial>()
                .eq(CourseMaterial::getCourseId, courseId)
                .eq(parentId == null, CourseMaterial::getParentId, parentId)
                .isNull(parentId != null, CourseMaterial::getParentId)
                .eq(CourseMaterial::getName, name.trim())
                .eq(CourseMaterial::getType, "folder"));
        if (count > 0) throw new BusinessException(400, "已存在同名文件夹");

        CourseMaterial folder = new CourseMaterial();
        folder.setCourseId(courseId);
        folder.setParentId(parentId);
        folder.setName(name.trim());
        folder.setType("folder");
        folder.setSortOrder(0);
        folder.setUserId(user.getUserId());
        folder.setCreateTime(LocalDateTime.now());
        folder.setUpdateTime(LocalDateTime.now());
        materialMapper.insert(folder);
        return Result.success(folder);
    }

    @Override
    @Transactional
    public Result<?> uploadFiles(Long courseId, Long parentId, MultipartFile[] files, User user) {
        checkTeacher(courseId, user.getUserId());
        if (files == null || files.length == 0) {
            throw new BusinessException(400, "请选择文件");
        }
        List<CourseMaterial> saved = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            try {
                String dirPath = UPLOAD_BASE + File.separator + "material";
                File dir = new File(dirPath);
                if (!dir.exists()) dir.mkdirs();

                String originalName = file.getOriginalFilename();
                String ext = "";
                if (originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf("."));
                }
                String newName = UUID.randomUUID() + ext;
                Path dest = Paths.get(dirPath, newName);
                Files.copy(file.getInputStream(), dest);

                CourseMaterial material = new CourseMaterial();
                material.setCourseId(courseId);
                material.setParentId(parentId);
                material.setName(originalName != null ? originalName : newName);
                material.setType("file");
                material.setFilePath("/uploads/material/" + newName);
                material.setFileSize(file.getSize());
                material.setSortOrder(0);
                material.setUserId(user.getUserId());
                material.setCreateTime(LocalDateTime.now());
                material.setUpdateTime(LocalDateTime.now());
                materialMapper.insert(material);
                saved.add(material);
            } catch (IOException e) {
                throw new BusinessException(500, "文件保存失败: " + e.getMessage());
            }
        }
        return Result.success(saved);
    }

    @Override
    @Transactional
    public Result<?> addLink(Long courseId, Long parentId, String name, String url, User user) {
        checkTeacher(courseId, user.getUserId());
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(400, "名称不能为空");
        }
        if (url == null || url.trim().isEmpty()) {
            throw new BusinessException(400, "链接地址不能为空");
        }
        // 外链自动补全协议
        String linkUrl = url.trim();
        if (!linkUrl.matches("(?i)^https?://.*")) {
            linkUrl = "https://" + linkUrl;
        }
        CourseMaterial link = new CourseMaterial();
        link.setCourseId(courseId);
        link.setParentId(parentId);
        link.setName(name.trim());
        link.setType("link");
        link.setUrl(linkUrl);
        link.setSortOrder(0);
        link.setUserId(user.getUserId());
        link.setCreateTime(LocalDateTime.now());
        link.setUpdateTime(LocalDateTime.now());
        materialMapper.insert(link);
        return Result.success(link);
    }

    @Override
    @Transactional
    public Result<?> deleteMaterial(Long materialId, User user) {
        CourseMaterial material = getOrThrow(materialId);
        checkTeacher(material.getCourseId(), user.getUserId());

        // 如果是文件夹，级联删除所有子项
        if ("folder".equals(material.getType())) {
            cascadeDelete(materialId);
        }
        // 如果是 file 类型，删除磁盘文件
        if ("file".equals(material.getType()) && material.getFilePath() != null) {
            try {
                File diskFile = new File(UPLOAD_BASE,
                        material.getFilePath().replace("/uploads/", "").replace("/", File.separator));
                if (diskFile.exists()) diskFile.delete();
            } catch (Exception ignored) {}
        }
        materialMapper.deleteById(materialId);
        return Result.success("删除成功");
    }

    /** 递归删除文件夹及其所有子项 */
    private void cascadeDelete(Long folderId) {
        List<CourseMaterial> children = materialMapper.selectList(
                new LambdaQueryWrapper<CourseMaterial>()
                        .eq(CourseMaterial::getParentId, folderId));
        for (CourseMaterial child : children) {
            if ("folder".equals(child.getType())) {
                cascadeDelete(child.getMaterialId());
            }
            if ("file".equals(child.getType()) && child.getFilePath() != null) {
                try {
                    File diskFile = new File(UPLOAD_BASE,
                            child.getFilePath().replace("/uploads/", "").replace("/", File.separator));
                    if (diskFile.exists()) diskFile.delete();
                } catch (Exception ignored) {}
            }
            materialMapper.deleteById(child.getMaterialId());
        }
    }

    @Override
    @Transactional
    public Result<?> moveMaterial(Long materialId, Long targetParentId, User user) {
        CourseMaterial material = getOrThrow(materialId);
        checkTeacher(material.getCourseId(), user.getUserId());

        // 不能把自己移到自己的子文件夹下
        if (targetParentId != null && "folder".equals(material.getType())) {
            if (isDescendantOf(targetParentId, materialId)) {
                throw new BusinessException(400, "不能移动到自己的子文件夹下");
            }
        }
        material.setParentId(targetParentId);
        material.setUpdateTime(LocalDateTime.now());
        materialMapper.updateById(material);
        return Result.success("移动成功");
    }

    /** 检查 candidateAncestor 是否是 materialId 的祖先 */
    private boolean isDescendantOf(Long candidateAncestor, Long materialId) {
        Long current = candidateAncestor;
        Set<Long> visited = new HashSet<>();
        while (current != null) {
            if (current.equals(materialId)) return true;
            if (!visited.add(current)) break; // 防止循环
            CourseMaterial parent = materialMapper.selectById(current);
            current = parent != null ? parent.getParentId() : null;
        }
        return false;
    }

    @Override
    @Transactional
    public Result<?> renameMaterial(Long materialId, String name, User user) {
        CourseMaterial material = getOrThrow(materialId);
        checkTeacher(material.getCourseId(), user.getUserId());
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(400, "名称不能为空");
        }
        material.setName(name.trim());
        material.setUpdateTime(LocalDateTime.now());
        materialMapper.updateById(material);
        return Result.success("重命名成功");
    }
}
