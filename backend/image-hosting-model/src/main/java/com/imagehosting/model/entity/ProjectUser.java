package com.imagehosting.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 项目用户实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ProjectUser {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 角色: 0-标注者, 1-管理员
     */
    private Integer role;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
