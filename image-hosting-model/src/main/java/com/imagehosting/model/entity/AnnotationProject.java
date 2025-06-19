package com.imagehosting.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 标注项目实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AnnotationProject {
    
    /**
     * 主键ID
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
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 项目状态: 0-进行中, 1-已完成, 2-已归档
     */
    private Integer status;
    
    /**
     * 项目图片数量
     */
    private Integer imageCount;
    
    /**
     * 已标注图片数量
     */
    private Integer annotatedCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
