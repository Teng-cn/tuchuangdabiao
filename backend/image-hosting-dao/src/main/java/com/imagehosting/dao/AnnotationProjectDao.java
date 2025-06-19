package com.imagehosting.dao;

import com.imagehosting.model.entity.AnnotationProject;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标注项目DAO接口
 */
public interface AnnotationProjectDao {
    /**
     * 插入项目
     *
     * @param project 项目
     * @return 影响行数
     */
    int insert(AnnotationProject project);

    /**
     * 根据ID查询项目
     *
     * @param id 项目ID
     * @return 项目
     */
    AnnotationProject findById(@Param("id") Long id);

    /**
     * 根据创建者ID查询项目列表
     *
     * @param creatorId 创建者ID
     * @return 项目列表
     */
    List<AnnotationProject> findByCreatorId(@Param("creatorId") Long creatorId);

    /**
     * 更新项目
     *
     * @param project 项目
     * @return 影响行数
     */
    int update(AnnotationProject project);

    /**
     * 删除项目
     *
     * @param id 项目ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);
    
    /**
     * 根据用户ID查询参与的项目列表
     *
     * @param userId 用户ID
     * @return 项目列表
     */
    List<AnnotationProject> findByUserId(@Param("userId") Long userId);
    
    /**
     * 统计项目总数
     *
     * @return 项目总数
     */
    Long countAll();

    /**
     * 更新项目已标注图片数量
     *
     * @param projectId 项目ID
     * @return 影响行数
     */
    int updateAnnotatedCount(@Param("projectId") Long projectId);
} 