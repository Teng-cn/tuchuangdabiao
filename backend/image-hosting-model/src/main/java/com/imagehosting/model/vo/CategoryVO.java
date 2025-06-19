package com.imagehosting.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标注类别VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO {
    /**
     * 类别ID
     */
    private Long id;
    
    /**
     * 类别名称
     */
    private String name;
    
    /**
     * 显示颜色
     */
    private String color;
} 