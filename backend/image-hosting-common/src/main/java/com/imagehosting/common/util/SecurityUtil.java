package com.imagehosting.common.util;

import com.imagehosting.common.exception.BusinessException;
import com.imagehosting.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * 提供与用户认证和安全相关的实用方法，用于获取当前登录用户信息
 */
@Slf4j
public class SecurityUtil {

    /**
     * 获取当前用户ID
     * 从Spring Security上下文中提取当前认证用户的ID
     *
     * @return 用户ID
     * @throws BusinessException 当用户未登录或无法获取用户ID时抛出异常
     */
    public static Long getCurrentUserId() {
        // 从SecurityContextHolder获取当前认证对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.error("获取用户ID失败: 认证对象为null");
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        
        // 检查用户是否已认证
        if (!authentication.isAuthenticated()) {
            log.error("获取用户ID失败: 用户未认证");
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        
        // 获取认证主体对象(通常包含用户标识)
        Object principal = authentication.getPrincipal();
        log.debug("认证主体类型: {}, 值: {}", principal != null ? principal.getClass().getName() : "null", principal);
        
        // 处理不同类型的主体对象
        if (principal instanceof Long) {
            // 直接是Long类型的用户ID
            Long userId = (Long) principal;
            log.debug("成功获取用户ID: {}", userId);
            return userId;
        } else if (principal instanceof String && ((String) principal).matches("\\d+")) {
            // 尝试将字符串转换为Long类型的用户ID
            try {
                Long userId = Long.parseLong((String) principal);
                log.debug("成功从字符串转换用户ID: {}", userId);
                return userId;
            } catch (NumberFormatException e) {
                log.error("将字符串转换为用户ID失败", e);
            }
        }
        
        // 无法获取有效的用户ID
        log.error("获取用户ID失败: 认证主体不是Long类型: {}", principal);
        throw new BusinessException(ResultCode.UNAUTHORIZED, "获取用户ID失败");
    }
    
    /**
     * 获取当前认证对象
     * 从Spring Security上下文中获取完整的认证信息
     *
     * @return 认证对象，如果未登录可能返回null
     */
    public static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.warn("获取认证对象失败: 认证对象为null");
        } else {
            log.debug("成功获取认证对象: {}", authentication);
        }
        return authentication;
    }
    
    /**
     * 判断当前用户是否已认证
     * 检查当前是否有用户登录并通过认证
     *
     * @return 如果用户已登录并认证返回true，否则返回false
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        boolean authenticated = authentication != null && authentication.isAuthenticated();
        log.debug("用户认证状态: {}", authenticated);
        return authenticated;
    }
} 