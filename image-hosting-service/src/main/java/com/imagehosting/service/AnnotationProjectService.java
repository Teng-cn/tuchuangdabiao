package com.imagehosting.service;

import com.imagehosting.model.dto.AnnotationDataDTO;
import com.imagehosting.model.dto.CreateProjectDTO;
import com.imagehosting.model.dto.SaveAnnotationDTO;
import com.imagehosting.model.vo.ImageVO;
import com.imagehosting.model.vo.PageVO;
import com.imagehosting.model.vo.ProjectDetailVO;
import com.imagehosting.model.vo.ProjectVO;

import java.util.List;
import java.util.Map;

/**
 * 标注项目服务接口
 */
public interface AnnotationProjectService {
    /**
     * 创建标注项目
     *
     * @param createProjectDTO 创建项目DTO
     * @return 项目ID
     */
    Long createProject(CreateProjectDTO createProjectDTO);
    
    /**
     * 获取项目详情
     *
     * @param projectId 项目ID
     * @return 项目详情
     */
    ProjectDetailVO getProjectDetail(Long projectId);
    
    /**
     * 添加图片到项目
     *
     * @param projectId 项目ID
     * @param imageIds 图片ID列表
     */
    void addImageToProject(Long projectId, List<Long> imageIds);
    
    /**
     * 添加用户到项目
     *
     * @param projectId 项目ID
     * @param userIds 用户ID列表
     */
    void addUserToProject(Long projectId, List<Long> userIds);
    
    /**
     * 保存标注数据
     *
     * @param annotationDataDTO 标注数据DTO
     */
    void saveAnnotation(AnnotationDataDTO annotationDataDTO);
    
    /**
     * 获取用户的项目列表
     *
     * @return 项目列表
     */
    List<ProjectVO> getUserProjects();
    
    /**
     * 获取项目的图片列表
     *
     * @param projectId 项目ID
     * @param page 页码
     * @param size 每页大小
     * @return 图片列表
     */
    PageVO<ImageVO> getProjectImages(Long projectId, Integer page, Integer size);
    
    /**
     * 打包项目数据
     *
     * @param projectId 项目ID
     * @return 数据包URL
     */
    String packageProjectData(Long projectId);
    
    /**
     * 完成项目
     *
     * @param projectId 项目ID
     */
    void completeProject(Long projectId);
    
    /**
     * 删除项目
     *
     * @param projectId 项目ID
     */
    void deleteProject(Long projectId);
    
    /**
     * 获取项目图片详情
     *
     * @param projectId 项目ID
     * @param imageId 图片ID
     * @return 图片详情
     */
    ImageVO getProjectImage(Long projectId, Long imageId);
    
    /**
     * 获取项目所有标注数据
     *
     * @param projectId 项目ID
     * @return 所有标注数据
     */
    List<Map<String, Object>> getProjectAnnotations(Long projectId);
    
    /**
     * 保存项目图片标注
     *
     * @param projectId 项目ID
     * @param imageId 图片ID
     * @param saveAnnotationDTO 标注数据
     */
    void saveImageAnnotation(Long projectId, Long imageId, SaveAnnotationDTO saveAnnotationDTO);
} 