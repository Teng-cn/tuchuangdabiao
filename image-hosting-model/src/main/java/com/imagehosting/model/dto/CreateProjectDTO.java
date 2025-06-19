package com.imagehosting.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建项目DTO
 */
@Data
public class CreateProjectDTO {
    /**
     * 项目名称
     */
    private String name;
    
    /**
     * 项目描述
     */
    private String description;
    
    /**
     * 参与标注的用户ID列表
     */
    private List<Long> userIds;
    
    /**
     * 标注类别列表
     */
    private List<String> categories;
} 