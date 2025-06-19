package com.imagehosting.dao;

import com.imagehosting.model.entity.AnnotationData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标注数据DAO接口
 */
public interface AnnotationDataDao {
    /**
     * 插入标注数据
     *
     * @param annotationData 标注数据
     * @return 影响行数
     */
    int insert(AnnotationData annotationData);

    /**
     * 根据ID查询标注数据
     *
     * @param id 标注数据ID
     * @return 标注数据
     */
    AnnotationData findById(@Param("id") Long id);

    /**
     * 根据项目ID和图片ID查询标注数据
     *
     * @param projectId 项目ID
     * @param imageId   图片ID
     * @return 标注数据
     */
    AnnotationData findByProjectIdAndImageId(@Param("projectId") Long projectId, @Param("imageId") Long imageId);

    /**
     * 根据项目ID查询标注数据列表
     *
     * @param projectId 项目ID
     * @return 标注数据列表
     */
    List<AnnotationData> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 更新标注数据
     *
     * @param annotationData 标注数据
     * @return 影响行数
     */
    int update(AnnotationData annotationData);

    /**
     * 删除标注数据
     *
     * @param id 标注数据ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);

    /**
     * 删除项目所有标注数据
     *
     * @param projectId 项目ID
     * @return 影响行数
     */
    int deleteByProjectId(@Param("projectId") Long projectId);

    /**
     * 删除图片的标注数据
     *
     * @param projectId 项目ID
     * @param imageId   图片ID
     * @return 影响行数
     */
    int deleteByProjectIdAndImageId(@Param("projectId") Long projectId, @Param("imageId") Long imageId);
} 