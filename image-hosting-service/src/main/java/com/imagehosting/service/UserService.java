package com.imagehosting.service;

import com.imagehosting.model.dto.LoginDTO;
import com.imagehosting.model.dto.RegisterDTO;
import com.imagehosting.model.dto.UpdatePasswordDTO;
import com.imagehosting.model.dto.UpdateUserDTO;
import com.imagehosting.model.entity.User;
import com.imagehosting.model.vo.LoginVO;
import com.imagehosting.model.vo.UserStatsVO;
import com.imagehosting.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务接口
 * 提供用户相关的业务逻辑操作
 * 包括用户注册、登录、信息查询与修改等功能
 */
public interface UserService {

    /**
     * 用户注册
     * 处理新用户的注册流程，包括信息验证、密码加密、数据存储等
     *
     * @param registerDTO 注册信息数据传输对象，包含用户名、密码、邮箱等注册信息
     * @throws com.imagehosting.common.exception.BusinessException 当用户名已存在等情况时抛出业务异常
     */
    void register(RegisterDTO registerDTO);

    /**
     * 用户登录
     * 验证用户身份并生成登录凭证（如JWT令牌）
     *
     * @param loginDTO 登录信息数据传输对象，包含用户名和密码
     * @return 登录结果视图对象，包含用户信息和访问令牌
     * @throws com.imagehosting.common.exception.BusinessException 当用户名或密码错误时抛出业务异常
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取当前用户信息
     * 根据认证上下文获取当前登录用户的详细信息
     *
     * @return 用户信息视图对象，不包含敏感信息如密码
     * @throws com.imagehosting.common.exception.BusinessException 当用户未登录或会话失效时抛出业务异常
     */
    UserVO getCurrentUserInfo();

    /**
     * 获取用户统计信息
     * 获取当前用户的相关统计数据，如上传图片数、存储空间使用量等
     *
     * @return 用户统计信息视图对象
     * @throws com.imagehosting.common.exception.BusinessException 当用户未登录或会话失效时抛出业务异常
     */
    UserStatsVO getUserStats();

    /**
     * 更新用户信息
     * 修改用户的基本信息，如昵称、邮箱、手机号等
     *
     * @param updateUserDTO 用户信息更新数据传输对象
     * @throws com.imagehosting.common.exception.BusinessException 当用户未登录或数据验证失败时抛出业务异常
     */
    void updateUserInfo(UpdateUserDTO updateUserDTO);

    /**
     * 修改密码
     * 验证旧密码并更新为新密码
     *
     * @param updatePasswordDTO 密码更新数据传输对象，包含旧密码和新密码
     * @throws com.imagehosting.common.exception.BusinessException 当旧密码验证失败时抛出业务异常
     */
    void updatePassword(UpdatePasswordDTO updatePasswordDTO);

    /**
     * 上传头像
     * 处理用户头像上传，包括图片验证、压缩、存储等
     *
     * @param file 头像图片文件
     * @return 头像的访问URL
     * @throws com.imagehosting.common.exception.BusinessException 当文件格式不支持或上传失败时抛出业务异常
     */
    String uploadAvatar(MultipartFile file);
} 