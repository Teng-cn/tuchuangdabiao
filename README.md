# 图床系统项目文档

## 1. 项目概述

这是一个基于前后端分离架构的图床系统，主要用于图片的上传、存储和管理，同时支持图片标注功能。系统采用现代化的技术栈，提供了完整的用户认证、图片管理、标注项目管理等功能。

## 2. 技术架构

### 2.1 后端架构
- 采用分层架构设计：
  - image-hosting-web：Web层，处理HTTP请求
  - image-hosting-service：业务逻辑层
  - image-hosting-dao：数据访问层
  - image-hosting-model：数据模型层
  - image-hosting-common：公共组件层

### 2.2 前端架构
- 基于Vue.js框架
- 使用TypeScript进行开发
- 采用Vite作为构建工具
- 使用Element Plus作为UI组件库

### 2.3 数据库设计
系统使用MySQL数据库，主要包含以下表：
- user：用户表
- image：图片表
- annotation_project：标注项目表
- project_image：项目图片关联表
- project_user：项目用户关联表
- annotation_data：标注数据表
- annotation_category：标注类别表

## 3. 核心功能

### 3.1 用户管理
- 用户注册
- 用户登录（支持普通用户和管理员）
- 个人信息管理
- 密码修改
- 头像上传

### 3.2 图片管理
- 图片上传
- 图片列表查看
- 图片删除
- 图片搜索
- 图片访问统计

### 3.3 标注项目管理
- 项目创建
- 项目成员管理
- 项目图片管理
- 标注数据管理
- 标注类别管理

## 4. 接口设计

系统提供RESTful API接口，主要包括：

### 4.1 认证接口
- POST /api/login：用户登录
- POST /api/user/register：用户注册

### 4.2 用户接口
- PUT /api/user/update：更新用户信息
- PUT /api/user/password：修改密码
- POST /api/user/avatar：上传头像

### 4.3 图片接口
- POST /api/image/upload：上传图片
- GET /api/image/list：获取图片列表
- DELETE /api/image/{id}：删除图片

### 4.4 标注项目接口
- 项目创建和管理
- 项目成员管理
- 标注数据管理
- 标注类别管理

## 5. 安全设计

- 使用JWT进行身份认证
- 密码加密存储
- 接口访问权限控制
- 文件上传安全控制

## 6. 部署要求

### 6.1 环境要求
- JDK 1.8+
- MySQL 5.7+
- Node.js 14+
- 现代浏览器支持

### 6.2 配置要求
- 数据库配置
- 文件存储配置
- 服务器配置

## 7. 开发规范

### 7.1 代码规范
- 遵循Java开发规范
- 遵循Vue.js开发规范
- 使用TypeScript进行前端开发

### 7.2 接口规范
- RESTful API设计规范
- 统一的响应格式
- 规范的错误处理

## 8. 项目特点

1. 完整的用户权限管理
2. 灵活的图片管理功能
3. 专业的图片标注功能
4. 现代化的技术栈
5. 良好的扩展性
6. 完善的文档支持

## 9. 未来规划

1. 支持更多图片格式
2. 增加图片处理功能
3. 优化标注工具
4. 增加数据分析功能
5. 提供更多API接口

## 10. 项目结构

### 10.1 后端结构
```
backend/
├── image-hosting-web/        # Web层
├── image-hosting-service/    # 业务逻辑层
├── image-hosting-dao/        # 数据访问层
├── image-hosting-model/      # 数据模型层
└── image-hosting-common/     # 公共组件层
```

### 10.2 前端结构
```
frontend/
├── src/
│   ├── shitu/               # 视图组件
│   ├── ziyuan/             # 资源文件
│   ├── gongju/             # 工具类
│   └── luyou/              # 路由配置
├── public/                 # 静态资源
└── dist/                   # 构建输出
```

## 11. 开发团队

- 后端开发：Java开发团队
- 前端开发：Vue.js开发团队
- 数据库设计：数据库团队
- 测试：QA团队

## 12. 版本历史

### v1.0.0
- 基础功能实现
- 用户管理
- 图片管理
- 标注功能

### v1.1.0
- 优化用户体验
- 增加数据分析
- 完善文档

## 13. 常见问题

1. 如何部署项目？
2. 如何配置数据库？
3. 如何修改配置？
4. 如何扩展功能？

## 14. 联系方式

- 技术支持：support@example.com
- 问题反馈：feedback@example.com
- 官方网站：www.example.com 
