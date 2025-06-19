package com.imagehosting.service.impl;

// 导入各种需要的类
import com.imagehosting.common.exception.BusinessException;
import com.imagehosting.common.result.ResultCode;
import com.imagehosting.common.util.JwtUtil;
import com.imagehosting.common.util.SecurityUtil;
import com.imagehosting.dao.ImageDao;
import com.imagehosting.dao.UserDao;
import com.imagehosting.model.dto.LoginDTO;
import com.imagehosting.model.dto.RegisterDTO;
import com.imagehosting.model.dto.UpdatePasswordDTO;
import com.imagehosting.model.dto.UpdateUserDTO;
import com.imagehosting.model.entity.User;
import com.imagehosting.model.vo.LoginVO;
import com.imagehosting.model.vo.UserStatsVO;
import com.imagehosting.model.vo.UserVO;
import com.imagehosting.service.StorageService;
import com.imagehosting.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户服务实现类 - 处理所有和用户相关的业务逻辑
 */
@Slf4j
@Service  // 告诉Spring这是一个服务类
@RequiredArgsConstructor  // 自动生成构造函数
public class UserServiceImpl implements UserService {

    // 依赖注入 - 用户数据库操作类
    private final UserDao userDao;
    // 依赖注入 - 图片数据库操作类
    private final ImageDao imageDao;
    // 依赖注入 - 密码加密工具
    private final PasswordEncoder passwordEncoder;
    // 依赖注入 - 存储服务
    private final StorageService storageService;
    // 依赖注入 - JWT工具类
    private final JwtUtil jwtUtil;

    /**
     * 注册功能 - 处理用户注册
     */
    @Override
    @Transactional  // 开启事务
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        if (userDao.findByUsername(registerDTO.getUsername()) != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名已存在");
        }

        // 创建用户实体对象
        User user = User.builder()
                .username(registerDTO.getUsername())  // 设置用户名
                .password(passwordEncoder.encode(registerDTO.getPassword()))  // 加密密码
                .email(registerDTO.getEmail())  // 设置邮箱
                .nickname(registerDTO.getUsername())  // 默认昵称和用户名相同
                .roleType(0) // 普通用户
                .status(0)   // 正常状态
                .createTime(LocalDateTime.now())  // 创建时间
                .updateTime(LocalDateTime.now())  // 更新时间
                .build();

        // 保存用户到数据库
        userDao.insert(user);
    }

    /**
     * 登录功能 - 处理用户登录
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 根据用户名查找用户
        User user = userDao.findByUsername(loginDTO.getUsername());

         // 检查用户是否存在
        if (user == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名或密码错误");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名或密码错误");
        }
        
        // 检查账号状态
        if (user.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "账号已被禁用");
        }

        // 构建返回给前端的用户信息
        UserVO userVO = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .roleType(user.getRoleType())
                .build();
                
        // 生成token
        String token = jwtUtil.generateToken(user.getId().toString());
        
        // 返回登录结果
        return LoginVO.builder()
                .user(userVO)
                .token(token)
                .build();


        // 特殊处理管理员账号
        /* boolean isAdminLogin = "admin".equals(loginDTO.getUsername()) && "admin123".equals(loginDTO.getPassword());
        
        // 普通用户必须存在且密码正确
        if (user == null && !isAdminLogin) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名或密码错误");
        }
        
        // 如果是管理员特殊登录
        if (isAdminLogin) {
            // 检查admin用户是否存在于数据库
            if (user == null) {
                // 创建默认管理员账户
                user = User.builder()
                        .id(1L)
                        .username("admin")
                        .nickname("管理员")
                        .password(passwordEncoder.encode("admin123"))
                        .roleType(1) // 管理员角色
                        .status(0)   // 正常状态
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                // 可以考虑在这里保存管理员账户到数据库
                log.info("使用默认管理员账户登录");
            }
        } else {
            // 普通用户验证密码
            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "用户名或密码错误");
            }
            
            // 检查账号状态
            if (user.getStatus() != 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "账号已被禁用");
            }
        }

        // 构建返回给前端的用户信息
        UserVO userVO = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .roleType(user.getRoleType())
                .build();
                
        // 生成token
        String token = jwtUtil.generateToken(user.getId().toString());
        
        // 返回登录结果
        return LoginVO.builder()
                .user(userVO)
                .token(token)
                .build(); */
    }

    /**
     * 获取当前用户信息
     */
    @Override
    public UserVO getCurrentUserInfo() {
        // 获取当前登录用户ID
        Long userId = SecurityUtil.getCurrentUserId();

        // 查询用户信息
        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 构建返回给前端的用户信息
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .roleType(user.getRoleType())
                .createTime(user.getCreateTime())
                .build();
    }

    /**
     * 获取用户统计数据
     */
    @Override
    public UserStatsVO getUserStats() {
        // 获取当前登录用户ID
        Long userId = SecurityUtil.getCurrentUserId();

        // 查询并构建用户统计数据
        return UserStatsVO.builder()
                .totalImages(imageDao.countByUserId(userId, null))  // 总图片数
                .totalStorage(imageDao.sumStorageSize())  // 总存储空间
                .totalAccess(imageDao.countAllImages(userId))  // 总访问量
                .todayUploaded(imageDao.countTodayUploaded())  // 今日上传数
                .build();
    }

    /**
     * 更新用户信息
     */
    @Override
    @Transactional  // 开启事务
    public void updateUserInfo(UpdateUserDTO updateUserDTO) {
        // 获取当前登录用户ID
        Long userId = SecurityUtil.getCurrentUserId();

        // 查询用户信息
        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 更新用户信息
        user.setNickname(updateUserDTO.getNickname());  // 更新昵称
        user.setEmail(updateUserDTO.getEmail());  // 更新邮箱
        user.setPhone(updateUserDTO.getPhone());  // 更新电话
        user.setUpdateTime(LocalDateTime.now());  // 更新时间

        // 保存更新到数据库
        userDao.update(user);
    }

    /**
     * 修改密码
     */
    @Override
    @Transactional  // 开启事务
    public void updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        // 获取当前登录用户ID
        Long userId = SecurityUtil.getCurrentUserId();

        // 查询用户信息
        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 验证旧密码是否正确
        if (!passwordEncoder.matches(updatePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));  //  更新密码
        user.setUpdateTime(LocalDateTime.now());  // 更新时间

        // 保存更新到数据库
        userDao.update(user);
    }

    /**
     * 上传头像
     */
    @Override
    @Transactional  // 开启事务
    public String uploadAvatar(MultipartFile file) {
        try {
            // 获取当前登录用户ID
            Long userId = SecurityUtil.getCurrentUserId();
    
            // 查询用户信息
            User user = userDao.findById(userId);
            if (user == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
            }
    
            // 检查上传的文件是否是图片
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "只能上传图片文件");
            }
    
            // 使用StorageService上传头像
            String filePath = storageService.uploadFile(file, "avatars/" + userId);
            
            // 获取头像访问URL
            String avatarUrl = storageService.getFileUrl(filePath);
    
            // 更新用户头像
            user.setAvatar(avatarUrl);
            user.setUpdateTime(LocalDateTime.now());
            userDao.update(user);
    
            return avatarUrl;
        } catch (IOException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "上传头像失败: " + e.getMessage());
        }
    }
} 