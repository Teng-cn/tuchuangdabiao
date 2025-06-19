package com.imagehosting.dao;

import com.imagehosting.model.entity.AnnotationCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标注类别DAO接口
 */
public interface AnnotationCategoryDao {
    
    /**
     * 插入类别
     */
    int insert(AnnotationCategory category);
    
    /**
     * 批量插入类别
     */
    int batchInsert(@Param("categories") List<AnnotationCategory> categories);
    
    /**
     * 根据项目ID查询类别列表
     */
    List<AnnotationCategory> selectByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 根据ID查询类别
     */
    AnnotationCategory selectById(@Param("id") Long id);
    
    /**
     * 更新类别
     */
    int update(AnnotationCategory category);
    
    /**
     * 删除类别
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据项目ID删除所有类别
     */
    int deleteByProjectId(@Param("projectId") Long projectId);
}
 