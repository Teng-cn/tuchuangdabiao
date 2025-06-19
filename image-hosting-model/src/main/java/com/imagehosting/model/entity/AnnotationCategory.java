package com.imagehosting.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标注类别实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationCategory {
    /**
     * ID
     */
    private Long id;
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 类别名称
     */
    private String name;
    
    /**
     * 类别颜色（十六进制颜色值，如 #FF0000）
     */
    private String color;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

