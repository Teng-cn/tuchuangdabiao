-- 创建数据库
-- 连接信息: 用户名=admin, 密码=lst123
CREATE DATABASE IF NOT EXISTS image_hosting DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE image_hosting;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `role_type` tinyint NOT NULL DEFAULT '0' COMMENT '角色类型（0普通用户，1管理员）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '帐号状态（0正常，1禁用）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 图片表
CREATE TABLE IF NOT EXISTS `image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(100) NOT NULL COMMENT '图片名称',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `path` varchar(255) NOT NULL COMMENT '存储路径',
  `url` varchar(255) NOT NULL COMMENT '访问URL',
  `md5` varchar(32) NOT NULL COMMENT 'MD5值',
  `size` bigint NOT NULL COMMENT '图片大小（字节）',
  `width` int DEFAULT NULL COMMENT '图片宽度',
  `height` int DEFAULT NULL COMMENT '图片高度',
  `mime_type` varchar(50) NOT NULL COMMENT '媒体类型',
  `access_count` bigint NOT NULL DEFAULT '0' COMMENT '访问次数',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0未删除，1已删除）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_md5` (`md5`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片表';

-- 标注项目表
CREATE TABLE IF NOT EXISTS `annotation_project` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `name` varchar(100) NOT NULL COMMENT '项目名称',
  `description` text COMMENT '项目描述',
  `creator_id` bigint NOT NULL COMMENT '创建者ID（管理员）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '项目状态（0进行中，1已完成，2已归档）',
  `image_count` int NOT NULL DEFAULT '0' COMMENT '项目图片数量',
  `annotated_count` int NOT NULL DEFAULT '0' COMMENT '已标注图片数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标注项目表';

-- 项目图片关联表
CREATE TABLE IF NOT EXISTS `project_image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `image_id` bigint NOT NULL COMMENT '图片ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '标注状态（0未标注，1已标注）',
  `annotated` tinyint NOT NULL DEFAULT '0' COMMENT '是否已标注（0未标注，1已标注）',
  `annotation_content` text COMMENT '标注内容（JSON格式）',
  `yolo_content` text COMMENT 'YOLO格式标注内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_image` (`project_id`, `image_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_image_id` (`image_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目图片关联表';

-- 项目用户关联表
CREATE TABLE IF NOT EXISTS `project_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色（0标注者，1管理员）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`, `user_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目用户关联表';

-- 标注数据表
CREATE TABLE IF NOT EXISTS `annotation_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `image_id` bigint NOT NULL COMMENT '图片ID',
  `user_id` bigint NOT NULL COMMENT '标注用户ID',
  `content` text NOT NULL COMMENT '标注内容（YOLOv格式）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_image` (`project_id`, `image_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_image_id` (`image_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标注数据表';

-- 标注类别表
CREATE TABLE IF NOT EXISTS `annotation_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '类别ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `name` varchar(50) NOT NULL COMMENT '类别名称',
  `color` varchar(7) DEFAULT '#000000' COMMENT '显示颜色',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_name` (`project_id`, `name`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标注类别表';

-- 添加外键约束
ALTER TABLE `image` 
  ADD CONSTRAINT `fk_image_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

ALTER TABLE `annotation_project` 
  ADD CONSTRAINT `fk_project_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

ALTER TABLE `project_image` 
  ADD CONSTRAINT `fk_pi_project` FOREIGN KEY (`project_id`) REFERENCES `annotation_project` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_pi_image` FOREIGN KEY (`image_id`) REFERENCES `image` (`id`) ON DELETE CASCADE;

ALTER TABLE `project_user` 
  ADD CONSTRAINT `fk_pu_project` FOREIGN KEY (`project_id`) REFERENCES `annotation_project` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_pu_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

ALTER TABLE `annotation_data` 
  ADD CONSTRAINT `fk_ad_project` FOREIGN KEY (`project_id`) REFERENCES `annotation_project` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_ad_image` FOREIGN KEY (`image_id`) REFERENCES `image` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_ad_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

ALTER TABLE `annotation_category` 
  ADD CONSTRAINT `fk_ac_project` FOREIGN KEY (`project_id`) REFERENCES `annotation_project` (`id`) ON DELETE CASCADE;

-- 插入初始管理员用户 (用户名: admin, 密码: admin123)
INSERT INTO `user` (`username`, `password`, `nickname`, `role_type`) 
VALUES ('admin', '$2a$10$Q27KFPPJ0IOTHE0iyTDOBOk7WBPRorI.uh6wHhQMIcMDa.FP4kRIe', '管理员', 1); 