package com.imagehosting.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标注数据实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationData {
    /**
     * ID
     */
    private Long id;
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 图片ID
     */
    private Long imageId;
    
    /**
     * 标注用户ID
     */
    private Long userId;
    
    /**
     * 标注内容（YOLOv格式）
     */
    private String content;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 