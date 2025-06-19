package com.imagehosting.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 用于存储系统中用户的基本信息
 * 对应数据库中的user表
 */
@Data               // 自动生成getter、setter、equals、hashCode和toString方法
@Builder            // 使用建造者模式创建对象
@NoArgsConstructor  // 生成无参构造函数
@AllArgsConstructor // 生成全参构造函数
public class User {
    
    /**
     * 用户ID
     * 唯一标识一个用户，数据库主键
     */
    private Long id;
    
    /**
     * 用户名
     * 用户登录账号，唯一
     */
    private String username;
    
    /**
     * 密码
     * 存储加密后的密码
     */
    private String password;
    
    /**
     * 昵称
     * 用户显示名称
     */
    private String nickname;
    
    /**
     * 邮箱
     * 用户联系邮箱，可用于找回密码
     */
    private String email;
    
    /**
     * 手机号
     * 用户联系电话
     */
    private String phone;
    
    /**
     * 头像URL
     * 用户头像图片的访问地址
     */
    private String avatar;
    
    /**
     * 角色类型
     * 用户角色权限类型：0-普通用户，1-管理员
     */
    private Integer roleType;
    
    /**
     * 帐号状态
     * 用户状态标志：0-正常，1-禁用
     */
    private Integer status;
    
    /**
     * 创建时间
     * 记录用户注册的时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     * 记录用户信息最后一次更新的时间
     */
    private LocalDateTime updateTime;
} 