package com.imagehosting.model.dto;

import lombok.Data;

/**
 * 标注数据DTO
 */
@Data
public class AnnotationDataDTO {
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 图片ID
     */
    private Long imageId;
    
    /**
     * 标注内容（YOLOv格式）
     */
    private String content;
} 