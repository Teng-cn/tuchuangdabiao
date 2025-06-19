package com.imagehosting.service.impl;

import com.imagehosting.common.exception.BusinessException;
import com.imagehosting.common.result.ResultCode;
import com.imagehosting.common.util.SecurityUtil;
import com.imagehosting.dao.*;
import com.imagehosting.model.dto.AnnotationDataDTO;
import com.imagehosting.model.dto.CreateProjectDTO;
import com.imagehosting.model.dto.SaveAnnotationDTO;
import com.imagehosting.model.entity.*;
import com.imagehosting.model.vo.*;
import com.imagehosting.service.AnnotationProjectService;
import com.imagehosting.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Map;
import java.util.HashMap;

/**
 * 标注项目服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnotationProjectServiceImpl implements AnnotationProjectService {
    private final AnnotationProjectDao projectDao;
    private final ProjectImageDao projectImageDao;
    private final ProjectUserDao projectUserDao;
    private final AnnotationDataDao annotationDataDao;
    private final AnnotationCategoryDao categoryDao;
    private final ImageDao imageDao;
    private final UserDao userDao;
    private final StorageService storageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProject(CreateProjectDTO createProjectDTO) {
        // 获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 查询用户信息，验证是否管理员
        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        
        if (user.getRoleType() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有管理员可以创建项目");
        }
        
        // 创建项目
        AnnotationProject project = AnnotationProject.builder()
                .name(createProjectDTO.getName())
                .description(createProjectDTO.getDescription())
                .creatorId(userId)
                .status(0) // 进行中
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        // 保存项目
        projectDao.insert(project);
        
        // 添加标注类别
        if (createProjectDTO.getCategories() != null && !createProjectDTO.getCategories().isEmpty()) {
            List<AnnotationCategory> categories = createProjectDTO.getCategories().stream()
                    .map(name -> AnnotationCategory.builder()
                            .projectId(project.getId())
                            .name(name)
                            .color(generateRandomColor())
                            .createTime(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());
            
            categoryDao.batchInsert(categories);
        }
        
        // 添加项目用户
        if (createProjectDTO.getUserIds() != null && !createProjectDTO.getUserIds().isEmpty()) {
            List<ProjectUser> projectUsers = createProjectDTO.getUserIds().stream()
                    .map(uid -> ProjectUser.builder()
                            .projectId(project.getId())
                            .userId(uid)
                            .role(0) // 标注者
                            .createTime(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());
            
            projectUserDao.batchInsert(projectUsers);
        }
        
        return project.getId();
    }

    @Override
    public ProjectDetailVO getProjectDetail(Long projectId) {
        // 查询项目
        AnnotationProject project = projectDao.findById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        
        // 统计图片数量
        int totalImages = projectImageDao.countByProjectId(projectId);
        int annotatedImages = projectImageDao.countAnnotatedByProjectId(projectId);
        
        // 查询标注者
        List<ProjectUser> projectUsers = projectUserDao.findByProjectId(projectId);
        List<UserVO> annotators = new ArrayList<>();
        for (ProjectUser projectUser : projectUsers) {
            User user = userDao.findById(projectUser.getUserId());
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                annotators.add(userVO);
            }
        }
        
        // 查询类别
        List<AnnotationCategory> categories = categoryDao.selectByProjectId(projectId);
        List<CategoryVO> categoryVOs = categories.stream()
                .map(category -> {
                    CategoryVO vo = new CategoryVO();
                    BeanUtils.copyProperties(category, vo);
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 组装返回结果
        return ProjectDetailVO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .createTime(project.getCreateTime())
                .totalImages(totalImages)
                .annotatedImages(annotatedImages)
                .annotators(annotators)
                .categories(categoryVOs)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addImageToProject(Long projectId, List<Long> imageIds) {
        // 验证项目存在
        AnnotationProject project = projectDao.findById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        
        // 验证是管理员或项目创建者
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userDao.findById(userId);
        if (user.getRoleType() != 1 && !project.getCreatorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限");
        }
        
        // 添加图片
        List<ProjectImage> projectImages = new ArrayList<>();
        for (Long imageId : imageIds) {
            // 检查图片是否已存在
            ProjectImage existingImage = projectImageDao.findByProjectIdAndImageId(projectId, imageId);
            if (existingImage == null) {
                projectImages.add(ProjectImage.builder()
                        .projectId(projectId)
                        .imageId(imageId)
                        .status(0) // 未标注
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build());
            }
        }
        
        if (!projectImages.isEmpty()) {
            projectImageDao.batchInsert(projectImages);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserToProject(Long projectId, List<Long> userIds) {
        // 验证项目存在
        AnnotationProject project = projectDao.findById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        
        // 验证是管理员或项目创建者
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userDao.findById(userId);
        if (user.getRoleType() != 1 && !project.getCreatorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限");
        }
        
        // 添加用户
        List<ProjectUser> projectUsers = new ArrayList<>();
        for (Long uid : userIds) {
            // 检查用户是否已存在
            ProjectUser existingUser = projectUserDao.findByProjectIdAndUserId(projectId, uid);
            if (existingUser == null) {
                projectUsers.add(ProjectUser.builder()
                        .projectId(projectId)
                        .userId(uid)
                        .role(0) // 标注者
                        .createTime(LocalDateTime.now())
                        .build());
            }
        }
        
        if (!projectUsers.isEmpty()) {
            projectUserDao.batchInsert(projectUsers);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAnnotation(AnnotationDataDTO annotationDataDTO) {
        // 验证项目存在
        AnnotationProject project = projectDao.findById(annotationDataDTO.getProjectId());
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        
        // 验证图片存在且属于该项目
        ProjectImage projectImage = projectImageDao.findByProjectIdAndImageId(
                annotationDataDTO.getProjectId(), annotationDataDTO.getImageId());
        if (projectImage == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "该图片不属于此项目");
        }
        
        // 获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 验证用户有权限标注（是管理员或项目创建者或项目标注者）
        User user = userDao.findById(userId);
        ProjectUser projectUser = projectUserDao.findByProjectIdAndUserId(annotationDataDTO.getProjectId(), userId);
        if (user.getRoleType() != 1 && !project.getCreatorId().equals(userId) && projectUser == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限标注此项目");
        }
        
        // 查询现有标注数据
        AnnotationData existingData = annotationDataDao.findByProjectIdAndImageId(
                annotationDataDTO.getProjectId(), annotationDataDTO.getImageId());
        
        if (existingData == null) {
            // 插入新标注数据
            AnnotationData annotationData = AnnotationData.builder()
                    .projectId(annotationDataDTO.getProjectId())
                    .imageId(annotationDataDTO.getImageId())
                    .userId(userId)
                    .content(annotationDataDTO.getContent())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            annotationDataDao.insert(annotationData);
        } else {
            // 更新标注数据
            existingData.setContent(annotationDataDTO.getContent());
            existingData.setUserId(userId);
            existingData.setUpdateTime(LocalDateTime.now());
            annotationDataDao.update(existingData);
        }
        
        // 更新图片标注状态
        projectImageDao.updateStatus(annotationDataDTO.getProjectId(), annotationDataDTO.getImageId(), 1);
    }

    @Override
    public List<ProjectVO> getUserProjects() {
        // 获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 获取用户角色
        User user = userDao.findById(userId);
        List<AnnotationProject> projects;
        
        if (user.getRoleType() == 1) {
            // 管理员查看所有项目
            projects = projectDao.findByCreatorId(userId);
        } else {
            // 普通用户查看参与的项目
            projects = projectDao.findByUserId(userId);
        }
        
        // 转换为VO
        return projects.stream()
                .map(project -> {
                    int totalImages = projectImageDao.countByProjectId(project.getId());
                    int annotatedImages = projectImageDao.countAnnotatedByProjectId(project.getId());
                    
                    return ProjectVO.builder()
                            .id(project.getId())
                            .name(project.getName())
                            .description(project.getDescription())
                            .status(project.getStatus())
                            .createTime(project.getCreateTime())
                            .totalImages(totalImages)
                            .annotatedImages(annotatedImages)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public PageVO<ImageVO> getProjectImages(Long projectId, Integer page, Integer size) {
        // 验证项目存在
        AnnotationProject project = projectDao.findById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        
        // 获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 验证用户有权限查看（是管理员或项目创建者或项目成员）
        User user = userDao.findById(userId);
        ProjectUser projectUser = projectUserDao.findByProjectIdAndUserId(projectId, userId);
        if (user.getRoleType() != 1 && !project.getCreatorId().equals(userId) && projectUser == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限查看此项目");
        }
        
        // 查询项目图片列表
        List<ProjectImage> projectImages = projectImageDao.findByProjectId(projectId);
        
        // 计算分页参数
        int start = (page - 1) * size;
        int end = Math.min(start + size, projectImages.size());
        List<ProjectImage> pagedProjectImages = projectImages.subList(start, end);
        
        // 查询图片详情
        List<ImageVO> imageVOs = new ArrayList<>();
        for (ProjectImage projectImage : pagedProjectImages) {
            Image image = imageDao.findById(projectImage.getImageId());
            if (image != null) {
                ImageVO imageVO = new ImageVO();
                BeanUtils.copyProperties(image, imageVO);
                
                // 设置标注状态
                imageVO.setAnnotated(projectImage.getStatus() == 1);
                
                // 查询标注数据
                AnnotationData annotationData = annotationDataDao.findByProjectIdAndImageId(projectId, image.getId());
                if (annotationData != null) {
                    imageVO.setAnnotationContent(annotationData.getContent());
                }
                
                imageVOs.add(imageVO);
            }
        }
        
        return PageVO.of(imageVOs, (long) projectImages.size(), page, size);
    }

    @Override
    public String packageProjectData(Long projectId) {
        // 验证项目存在
        AnnotationProject project = projectDao.findById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        
        // 获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 验证是管理员或项目创建者
        User user = userDao.findById(userId);
        if (user.getRoleType() != 1 && !project.getCreatorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限打包项目数据");
        }
        
        // 获取项目中所有图片
        List<ProjectImage> projectImages = projectImageDao.findByProjectId(projectId);
        
        if (projectImages.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目中没有图片，无法打包");
        }
        
        log.info("开始打包项目 {} 的数据，共 {} 张图片", projectId, projectImages.size());
        
        // 创建临时目录
        String tempDir = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString();
        File dir = new File(tempDir);
        File imagesDir = new File(tempDir + File.separator + "images");
        File labelsDir = new File(tempDir + File.separator + "labels");
        
        log.info("创建临时目录: {}", tempDir);
        
        try {
            // 确保目录创建成功
            if (!dir.mkdirs()) {
                log.warn("创建临时目录失败: {}", tempDir);
                // 尝试再次创建
                if (!dir.exists()) {
                    throw new BusinessException(ResultCode.SYSTEM_ERROR, "创建临时目录失败");
                }
            }
            
            if (!imagesDir.mkdirs()) {
                log.warn("创建图片目录失败: {}", imagesDir.getPath());
                if (!imagesDir.exists()) {
                    throw new BusinessException(ResultCode.SYSTEM_ERROR, "创建图片目录失败");
                }
            }
            
            if (!labelsDir.mkdirs()) {
                log.warn("创建标签目录失败: {}", labelsDir.getPath());
                if (!labelsDir.exists()) {
                    throw new BusinessException(ResultCode.SYSTEM_ERROR, "创建标签目录失败");
                }
            }
            
            // 下载图片和标注数据
            int imageCount = 0;
            int annotationCount = 0;
            
            for (ProjectImage projectImage : projectImages) {
                Image image = imageDao.findById(projectImage.getImageId());
                if (image != null) {
                    try {
                        // 处理文件名，确保安全
                        String filename = image.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
                        
                        // 下载图片
                        URL url = new URL(image.getUrl());
                        File imageFile = new File(imagesDir, filename);
                        
                        log.debug("正在下载图片: {}", image.getUrl());
                        try (var inputStream = url.openStream()) {
                            Files.copy(inputStream, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            imageCount++;
                            log.debug("图片下载成功: {}", filename);
                        }
                        
                        // 获取文件名的基本部分（不含扩展名）
                        String baseName = filename;
                        int lastDot = filename.lastIndexOf('.');
                        if (lastDot > 0) {
                            baseName = filename.substring(0, lastDot);
                        }
                        
                        // 保存标注数据
                        String labelFilename = baseName + ".txt";
                        File labelFile = new File(labelsDir, labelFilename);
                        
                        // 检查标注数据
                        log.debug("检查图片 {} 的标注数据", image.getId());
                        log.debug("- 标注状态: {}", projectImage.getAnnotated());
                        log.debug("- YOLO内容: {}", projectImage.getYoloContent() != null ? 
                                 (projectImage.getYoloContent().length() > 100 ? 
                                  projectImage.getYoloContent().substring(0, 100) + "..." : 
                                  projectImage.getYoloContent()) : "null");
                        
                        // 直接使用projectImage中的yoloContent
                        if (projectImage.getYoloContent() != null && !projectImage.getYoloContent().isEmpty()) {
                            Files.writeString(labelFile.toPath(), projectImage.getYoloContent());
                            log.debug("已保存标注数据到: {}", labelFile.getPath());
                            annotationCount++;
                        } else {
                            log.warn("图片 {} 没有YOLO格式标注数据", image.getId());
                            
                            // 创建一个空的标注文件，确保文件存在
                            Files.writeString(labelFile.toPath(), "");
                            log.debug("已创建空标注文件: {}", labelFile.getPath());
                        }
                    } catch (Exception e) {
                        log.error("处理图片 {} 失败: {}", image.getId(), e.getMessage(), e);
                    }
                }
            }
            
            log.info("成功下载 {} 张图片，包含 {} 个有效标注", imageCount, annotationCount);
            
            if (imageCount == 0) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "没有成功下载任何图片，打包失败");
            }
            
            // 打包为zip
            String zipFilePath = tempDir + ".zip";
            File zipFile = new File(zipFilePath);
            
            log.info("开始创建ZIP文件: {}", zipFilePath);
            
            // 使用更可靠的方式创建ZIP文件
            try {
                // 创建一个缓冲输出流
                try (FileOutputStream fos = new FileOutputStream(zipFile);
                     BufferedOutputStream bos = new BufferedOutputStream(fos);
                     ZipOutputStream zos = new ZipOutputStream(bos)) {
                    
                    // 设置压缩级别
                    zos.setLevel(ZipOutputStream.STORED); // 不压缩，只存储
                    
                    // 添加images目录
                    addDirectoryToZip(imagesDir, "images", zos);
                    
                    // 添加labels目录
                    addDirectoryToZip(labelsDir, "labels", zos);
                    
                    // 确保所有数据都写入
                    zos.flush();
                }
            } catch (Exception e) {
                log.error("创建ZIP文件失败", e);
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "创建ZIP文件失败: " + e.getMessage());
            }
            
            log.info("ZIP文件创建成功，大小: {} bytes", zipFile.length());
            
            // 上传到OSS
            log.info("开始上传ZIP文件到OSS");
            String ossPath = storageService.uploadFile(zipFile, "packages");
            log.info("ZIP文件上传成功，OSS路径: {}", ossPath);
            
            // 获取下载URL
            String downloadUrl = storageService.getFileUrl(ossPath);
            log.info("生成下载URL: {}", downloadUrl);
            
            // 清理临时文件
            try {
                FileUtils.deleteDirectory(dir);
                FileUtils.deleteQuietly(zipFile);
                log.info("临时文件清理完成");
            } catch (Exception e) {
                log.warn("清理临时文件失败: {}", e.getMessage());
                // 继续执行，不影响结果
            }
            
            return downloadUrl;
        } catch (IOException e) {
            log.error("打包项目数据失败", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "打包项目数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 将目录添加到ZIP文件中
     *
     * @param folder 要添加的目录
     * @param pathInZip ZIP文件中的路径
     * @param zos ZIP输出流
     * @throws IOException IO异常
     */
    private void addDirectoryToZip(File folder, String pathInZip, ZipOutputStream zos) throws IOException {
        File[] files = folder.listFiles();
        if (files != null) {
            byte[] buffer = new byte[8192]; // 8KB buffer
            int bytesRead;
            
            for (File file : files) {
                if (file.isDirectory()) {
                    addDirectoryToZip(file, pathInZip + "/" + file.getName(), zos);
                } else {
                    try (FileInputStream fis = new FileInputStream(file);
                         BufferedInputStream bis = new BufferedInputStream(fis)) {
                        
                        // 创建新的ZIP条目
                        ZipEntry zipEntry = new ZipEntry(pathInZip + "/" + file.getName());
                        zos.putNextEntry(zipEntry);
                        
                        // 写入文件内容
                        while ((bytesRead = bis.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                        
                        zos.closeEntry();
                    }
                }
            }
        }
    }
    
    /**
     * 压缩目录
     *
     * @param folder 要压缩的目录
     * @param parentFolder 父目录名称
     * @param zipOut 压缩输出流
     * @throws IOException IO异常
     */
    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zipOut) throws IOException {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    zipDirectory(file, parentFolder + "/" + file.getName(), zipOut);
                } else {
                    ZipEntry zipEntry = new ZipEntry(parentFolder + "/" + file.getName());
                    zipOut.putNextEntry(zipEntry);
                    Files.copy(file.toPath(), zipOut);
                    zipOut.closeEntry();
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeProject(Long projectId) {
        // 验证项目存在
        AnnotationProject project = projectDao.findById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        
        // 获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 验证是管理员或项目创建者
        User user = userDao.findById(userId);
        if (user.getRoleType() != 1 && !project.getCreatorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限完成项目");
        }
        
        // 检查是否所有图片都已标注
        int totalImages = projectImageDao.countByProjectId(projectId);
        int annotatedImages = projectImageDao.countAnnotatedByProjectId(projectId);
        
        if (totalImages > 0 && annotatedImages < totalImages) {
            throw new BusinessException(ResultCode.PARAM_ERROR, 
                    "项目尚未完成标注，共" + totalImages + "张图片，已标注" + annotatedImages + "张");
        }
        
        // 更新项目状态
        project.setStatus(1); // 已完成
        project.setUpdateTime(LocalDateTime.now());
        projectDao.update(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long projectId) {
        // 验证项目存在
        AnnotationProject project = projectDao.findById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        
        // 获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 验证是管理员或项目创建者
        User user = userDao.findById(userId);
        if (user.getRoleType() != 1 && !project.getCreatorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限删除项目");
        }
        
        // 删除项目相关数据
        annotationDataDao.deleteByProjectId(projectId);
        categoryDao.deleteByProjectId(projectId);
        projectImageDao.deleteByProjectId(projectId);
        projectUserDao.deleteByProjectId(projectId);
        
        // 删除项目
        projectDao.delete(projectId);
    }

    @Override
    public ImageVO getProjectImage(Long projectId, Long imageId) {
        // 验证项目存在
        AnnotationProject project = projectDao.findById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        
        // 验证用户权限
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 检查用户是否是项目创建者或标注者
        boolean isCreator = project.getCreatorId().equals(userId);
        boolean isAnnotator = projectUserDao.existsByProjectIdAndUserId(projectId, userId);
        
        if (!isCreator && !isAnnotator) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该项目图片");
        }
        
        // 验证图片是否属于项目
        ProjectImage projectImage = projectImageDao.findByProjectIdAndImageId(projectId, imageId);
        if (projectImage == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "图片不存在或不属于该项目");
        }
        
        log.info("获取项目图片: projectId={}, imageId={}, projectImage={}", projectId, imageId, projectImage);
        
        // 获取图片信息
        Image image = imageDao.findById(imageId);
        if (image == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "图片不存在");
        }
        
        // 转换为VO
        ImageVO imageVO = new ImageVO();
        BeanUtils.copyProperties(image, imageVO);
        
        // 添加标注信息
        if (projectImage.getAnnotationContent() != null) {
            log.info("图片标注内容: {}", projectImage.getAnnotationContent());
            imageVO.setAnnotationContent(projectImage.getAnnotationContent());
            imageVO.setAnnotated(true);
        } else {
            log.info("图片没有标注内容");
            imageVO.setAnnotated(false);
        }
        
        return imageVO;
    }

    @Override
    public List<Map<String, Object>> getProjectAnnotations(Long projectId) {
        // 验证项目存在
        AnnotationProject project = projectDao.findById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        
        // 验证用户权限
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 检查用户是否是项目创建者或标注者
        boolean isCreator = project.getCreatorId().equals(userId);
        boolean isAnnotator = projectUserDao.existsByProjectIdAndUserId(projectId, userId);
        
        if (!isCreator && !isAnnotator) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该项目数据");
        }
        
        // 获取项目所有图片
        List<ProjectImage> projectImages = projectImageDao.findByProjectId(projectId);
        
        // 获取项目类别
        List<AnnotationCategory> categories = categoryDao.selectByProjectId(projectId);
        
        // 记录日志
        log.info("获取项目标注数据: projectId={}, 图片数量={}, 类别数量={}", 
                projectId, projectImages.size(), categories.size());
        
        // 构建返回结果
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ProjectImage projectImage : projectImages) {
            // 跳过未标注的图片
            if (projectImage.getAnnotationContent() == null || projectImage.getAnnotationContent().isEmpty()) {
                continue;
            }
            
            // 获取图片信息
            Image image = imageDao.findById(projectImage.getImageId());
            if (image == null) {
                continue;
            }
            
            try {
                // 解析标注内容
                String annotationContent = projectImage.getAnnotationContent();
                List<Map<String, Object>> annotations = new ArrayList<>();
                
                // 尝试解析JSON格式的标注内容
                ObjectMapper objectMapper = new ObjectMapper();
                List<Map<String, Object>> annotationList = objectMapper.readValue(
                    annotationContent, 
                    new TypeReference<List<Map<String, Object>>>() {}
                );
                
                log.info("图片 {} 标注数据: {}", image.getName(), annotationContent);
                
                for (Map<String, Object> annotation : annotationList) {
                    // 获取类别信息
                    Integer categoryId = (Integer) annotation.get("categoryId");
                    Integer categoryIndex = -1;
                    String categoryName = "未知类别";
                    
                    // 查找类别索引和名称
                    for (int i = 0; i < categories.size(); i++) {
                        AnnotationCategory category = categories.get(i);
                        if (category.getId().intValue() == categoryId.intValue()) {
                            categoryIndex = i;
                            categoryName = category.getName();
                            break;
                        }
                    }
                    
                    // 构建标注信息
                    Map<String, Object> annotationInfo = new HashMap<>();
                    annotationInfo.put("categoryId", categoryId);
                    annotationInfo.put("categoryIndex", categoryIndex);
                    annotationInfo.put("categoryName", categoryName);
                    
                    // 计算中心点和宽高
                    double x = ((Number) annotation.get("x")).doubleValue();
                    double y = ((Number) annotation.get("y")).doubleValue();
                    double width = ((Number) annotation.get("width")).doubleValue();
                    double height = ((Number) annotation.get("height")).doubleValue();
                    
                    double centerX = x + width / 2;
                    double centerY = y + height / 2;
                    
                    annotationInfo.put("centerX", centerX);
                    annotationInfo.put("centerY", centerY);
                    annotationInfo.put("width", width);
                    annotationInfo.put("height", height);
                    annotationInfo.put("x", x);
                    annotationInfo.put("y", y);
                    
                    annotations.add(annotationInfo);
                }
                
                // 构建图片标注信息
                Map<String, Object> imageAnnotation = new HashMap<>();
                imageAnnotation.put("id", image.getId());
                imageAnnotation.put("name", image.getName());
                imageAnnotation.put("url", image.getUrl());
                imageAnnotation.put("annotations", annotations);
                
                result.add(imageAnnotation);
            } catch (Exception e) {
                log.error("解析标注内容失败: {}", e.getMessage());
            }
        }
        
        log.info("返回标注数据: 图片数量={}", result.size());
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveImageAnnotation(Long projectId, Long imageId, SaveAnnotationDTO saveAnnotationDTO) {
        // 验证项目存在
        AnnotationProject project = projectDao.findById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "项目不存在");
        }
        
        // 验证用户权限
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 检查用户是否是项目创建者或标注者
        boolean isCreator = project.getCreatorId().equals(userId);
        boolean isAnnotator = projectUserDao.existsByProjectIdAndUserId(projectId, userId);
        
        if (!isCreator && !isAnnotator) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该项目图片");
        }
        
        // 验证图片是否属于项目
        ProjectImage projectImage = projectImageDao.findByProjectIdAndImageId(projectId, imageId);
        if (projectImage == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "图片不存在或不属于该项目");
        }
        
        log.info("保存前的项目图片: {}", projectImage);
        
        // 更新标注内容
        projectImage.setAnnotationContent(saveAnnotationDTO.getAnnotationContent());
        projectImage.setYoloContent(saveAnnotationDTO.getYoloContent());
        projectImage.setAnnotated(1); // 已标注
        projectImage.setStatus(1); // 确保状态也设置为已标注
        projectImage.setUpdateTime(LocalDateTime.now());
        
        // 记录日志
        log.info("保存图片标注: projectId={}, imageId={}, annotationContent={}",
                projectId, imageId, saveAnnotationDTO.getAnnotationContent());
        
        // 更新项目图片
        int updated = projectImageDao.update(projectImage);
        log.info("更新项目图片结果: {}, 更新后的项目图片: {}", updated, projectImage);
        
        // 更新项目已标注图片数量
        projectDao.updateAnnotatedCount(projectId);
        
        // 记录标注数据
        AnnotationData existingData = annotationDataDao.findByProjectIdAndImageId(projectId, imageId);
        if (existingData == null) {
            // 插入新标注数据
            AnnotationData annotationData = AnnotationData.builder()
                    .projectId(projectId)
                    .imageId(imageId)
                    .userId(userId)
                    .content(saveAnnotationDTO.getYoloContent())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            annotationDataDao.insert(annotationData);
            log.info("插入新标注数据: {}", annotationData);
        } else {
            // 更新标注数据
            existingData.setContent(saveAnnotationDTO.getYoloContent());
            existingData.setUserId(userId);
            existingData.setUpdateTime(LocalDateTime.now());
            annotationDataDao.update(existingData);
            log.info("更新标注数据: {}", existingData);
        }
    }
    
    /**
     * 生成随机颜色
     *
     * @return 颜色十六进制值，如 #FF0000
     */
    private String generateRandomColor() {
        String[] colors = {
                "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF",
                "#FFA500", "#800080", "#008000", "#000080", "#800000", "#808000"
        };
        return colors[(int) (Math.random() * colors.length)];
    }
} 