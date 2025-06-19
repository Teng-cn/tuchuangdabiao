package com.imagehosting.dao;

import com.imagehosting.model.entity.ProjectUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目用户关联DAO接口
 */
public interface ProjectUserDao {
    /**
     * 插入项目用户关联
     *
     * @param projectUser 项目用户关联
     * @return 影响行数
     */
    int insert(ProjectUser projectUser);

    /**
     * 批量插入项目用户关联
     *
     * @param projectUsers 项目用户关联列表
     * @return 影响行数
     */
    int batchInsert(@Param("projectUsers") List<ProjectUser> projectUsers);

    /**
     * 根据项目ID和用户ID查询项目用户关联
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 项目用户关联
     */
    ProjectUser findByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 根据项目ID查询项目用户关联列表
     *
     * @param projectId 项目ID
     * @return 项目用户关联列表
     */
    List<ProjectUser> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据用户ID查询项目用户关联列表
     *
     * @param userId 用户ID
     * @return 项目用户关联列表
     */
    List<ProjectUser> findByUserId(@Param("userId") Long userId);

    /**
     * 删除项目用户关联
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 影响行数
     */
    int delete(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 删除项目所有用户关联
     *
     * @param projectId 项目ID
     * @return 影响行数
     */
    int deleteByProjectId(@Param("projectId") Long projectId);

    /**
     * 检查项目用户关联是否存在
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 是否存在
     */
    boolean existsByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);
} 