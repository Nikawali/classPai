package org.example.classpai.service;

import org.example.classpai.common.Result;
import org.example.classpai.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface CourseMaterialService {
    /** 列出指定目录下的资料（文件夹在前，按 sort_order 排序） */
    Result<?> listMaterials(Long courseId, Long parentId, User user);

    /** 创建文件夹（仅教师） */
    Result<?> createFolder(Long courseId, Long parentId, String name, User user);

    /** 上传附件（仅教师） */
    Result<?> uploadFiles(Long courseId, Long parentId, MultipartFile[] files, User user);

    /** 添加外链（仅教师） */
    Result<?> addLink(Long courseId, Long parentId, String name, String url, User user);

    /** 删除资料/文件夹（仅教师，删除文件夹会级联删除子项） */
    Result<?> deleteMaterial(Long materialId, User user);

    /** 移动资料到目标文件夹（仅教师） */
    Result<?> moveMaterial(Long materialId, Long targetParentId, User user);

    /** 重命名（仅教师） */
    Result<?> renameMaterial(Long materialId, String name, User user);
}
