package com.imagehosting.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectVO {
    /**
     * 项目ID
     */
    private Long id;
    
    /**
     * 项目名称
     */
    private String name;
    
    /**
     * 项目描述
     */
    private String description;
    
    /**
     * 项目状态（0进行中，1已完成）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 总图片数
     */
    private Integer totalImages;
    
    /**
     * 已标注图片数
     */
    private Integer annotatedImages;
} 