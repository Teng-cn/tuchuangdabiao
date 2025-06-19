package com.imagehosting.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 保存标注数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveAnnotationDTO {
    /**
     * 标注内容（JSON格式）
     */
    private String annotationContent;
    
    /**
     * YOLO格式标注内容
     */
    private String yoloContent;
} 