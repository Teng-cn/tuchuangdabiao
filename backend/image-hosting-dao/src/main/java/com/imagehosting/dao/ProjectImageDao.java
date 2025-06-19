package com.imagehosting.dao;

import com.imagehosting.model.entity.ProjectImage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目图片关联DAO接口
 */
public interface ProjectImageDao {
    /**
     * 插入项目图片关联
     *
     * @param projectImage 项目图片关联
     * @return 影响行数
     */
    int insert(ProjectImage projectImage);

    /**
     * 批量插入项目图片关联
     *
     * @param projectImages 项目图片关联列表
     * @return 影响行数
     */
    int batchInsert(@Param("projectImages") List<ProjectImage> projectImages);

    /**
     * 根据项目ID和图片ID查询项目图片关联
     *
     * @param projectId 项目ID
     * @param imageId   图片ID
     * @return 项目图片关联
     */
    ProjectImage findByProjectIdAndImageId(@Param("projectId") Long projectId, @Param("imageId") Long imageId);

    /**
     * 根据项目ID查询项目图片关联列表
     *
     * @param projectId 项目ID
     * @return 项目图片关联列表
     */
    List<ProjectImage> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目ID查询已标注的项目图片关联列表
     *
     * @param projectId 项目ID
     * @return 已标注的项目图片关联列表
     */
    List<ProjectImage> findAnnotatedByProjectId(@Param("projectId") Long projectId);

    /**
     * 更新项目图片关联状态
     *
     * @param projectId 项目ID
     * @param imageId   图片ID
     * @param status    状态
     * @return 影响行数
     */
    int updateStatus(@Param("projectId") Long projectId, @Param("imageId") Long imageId, @Param("status") Integer status);

    /**
     * 删除项目图片关联
     *
     * @param projectId 项目ID
     * @param imageId   图片ID
     * @return 影响行数
     */
    int delete(@Param("projectId") Long projectId, @Param("imageId") Long imageId);

    /**
     * 删除项目所有图片关联
     *
     * @param projectId 项目ID
     * @return 影响行数
     */
    int deleteByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计项目图片数
     *
     * @param projectId 项目ID
     * @return 图片数
     */
    int countByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计项目已标注图片数
     *
     * @param projectId 项目ID
     * @return 已标注图片数
     */
    int countAnnotatedByProjectId(@Param("projectId") Long projectId);

    /**
     * 更新项目图片关联
     *
     * @param projectImage 项目图片关联
     * @return 影响行数
     */
    int update(ProjectImage projectImage);
} 