package com.imagehosting.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图片VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageVO {
    
    /**
     * 图片ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 图片名称
     */
    private String name;
    
    /**
     * 访问URL
     */
    private String url;
    
    /**
     * 图片大小（字节）
     */
    private Long size;
    
    /**
     * 图片宽度
     */
    private Integer width;
    
    /**
     * 图片高度
     */
    private Integer height;
    
    /**
     * 媒体类型
     */
    private String mimeType;
    
    /**
     * 访问次数
     */
    private Long accessCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 是否已标注
     */
    private Boolean annotated;
    
    /**
     * 标注内容
     */
    private String annotationContent;
} 