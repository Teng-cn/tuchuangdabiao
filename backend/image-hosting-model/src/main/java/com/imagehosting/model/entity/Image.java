package com.imagehosting.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图片实体类
 * 用于存储图床系统中图片的相关信息
 * 对应数据库中的image表
 */
@Data               // 自动生成getter、setter、equals、hashCode和toString方法
@Builder            // 使用建造者模式创建对象
@NoArgsConstructor  // 生成无参构造函数
@AllArgsConstructor // 生成全参构造函数
public class Image {
    
    /**
     * 图片ID
     * 唯一标识一张图片，数据库主键
     */
    private Long id;
    
    /**
     * 用户ID
     * 图片所属用户的ID，关联用户表
     */
    private Long userId;
    
    /**
     * 图片名称
     * 系统生成的唯一图片名称
     */
    private String name;
    
    /**
     * 原始文件名
     * 用户上传时的原始文件名
     */
    private String originalName;
    
    /**
     * 存储路径
     * 图片在存储系统中的相对路径
     */
    private String path;
    
    /**
     * 访问URL
     * 图片的公开访问地址
     */
    private String url;
    
    /**
     * MD5值
     * 图片文件的MD5哈希值，用于去重判断
     */
    private String md5;
    
    /**
     * 图片大小
     * 图片文件的大小，单位为字节
     */
    private Long size;
    
    /**
     * 图片宽度
     * 图片的像素宽度
     */
    private Integer width;
    
    /**
     * 图片高度
     * 图片的像素高度
     */
    private Integer height;
    
    /**
     * 媒体类型
     * 图片的MIME类型，如image/jpeg、image/png等
     */
    private String mimeType;
    
    /**
     * 访问次数
     * 记录图片被访问的总次数
     */
    private Long accessCount;
    
    /**
     * 删除标志
     * 逻辑删除标记：0-未删除，1-已删除
     */
    private Integer deleted;
    
    /**
     * 创建时间
     * 图片上传的时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     * 图片信息最后一次更新的时间
     */
    private LocalDateTime updateTime;
} 