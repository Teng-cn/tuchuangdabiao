package com.imagehosting.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 项目图片实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ProjectImage {
    
    /**
     * 主键ID
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
     * 是否已标注: 0-未标注, 1-已标注
     */
    private Integer annotated;
    
    /**
     * 状态: 0-未标注, 1-已标注
     */
    private Integer status;
    
    /**
     * 标注内容 (JSON格式)
     */
    private String annotationContent;
    
    /**
     * YOLO格式标注内容
     */
    private String yoloContent;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
