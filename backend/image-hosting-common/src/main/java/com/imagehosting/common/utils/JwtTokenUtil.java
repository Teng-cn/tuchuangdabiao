package com.imagehosting.common.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 
 * JWT(JSON Web Token)是一种开放标准，用于在各方之间安全地传输信息。
 * 本工具类提供JWT令牌的生成、解析和验证功能，用于系统的用户认证和授权。
 * 
 * JWT令牌结构：
 * 1. 头部(Header): 包含令牌类型和签名算法
 * 2. 载荷(Payload): 包含用户信息和元数据
 * 3. 签名(Signature): 用于验证令牌的完整性和真实性
 */
@Slf4j
@Component
public class JwtTokenUtil {
    /**
     * JWT密钥
     * 用于对令牌进行签名，确保令牌的安全性
     */
    @Value("${jwt.secret:imageHostingSecret}")
    private String secret;

    /**
     * JWT过期时间（秒）
     * 默认为86400秒(24小时)
     */
    @Value("${jwt.expiration:86400}")
    private Long expiration;

    /**
     * JWT头部前缀
     * 用于HTTP请求头中的Authorization字段
     * 例如：Authorization: Bearer xxx.yyy.zzz
     */
    @Value("${jwt.tokenHead:Bearer}")
    private String tokenHead;

    /**
     * 生成JWT密钥
     * 使用HMAC-SHA算法创建签名密钥
     * 
     * @return 签名密钥对象
     */
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 根据用户信息生成token
     * 将用户名和用户ID封装到JWT令牌中
     *
     * @param username 用户名，用于身份识别
     * @param userId   用户ID，用于资源授权
     * @return 生成的JWT令牌字符串
     */
    public String generateToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>(2);
        claims.put("sub", username);      // sub是JWT标准声明，表示主题(Subject)
        claims.put("userId", userId);      // 自定义声明，存储用户ID
        claims.put("created", new Date()); // 记录令牌创建时间
        return generateToken(claims);
    }

    /**
     * 从token中获取登录用户名
     * 解析JWT令牌并提取其中的用户名信息
     *
     * @param token JWT令牌
     * @return 用户名，如果解析失败则返回null
     */
    public String getUsernameFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = claims.getSubject(); // 从标准声明sub中获取用户名
        } catch (Exception e) {
            log.error("从token中获取用户名失败", e);
            username = null;
        }
        return username;
    }

    /**
     * 从token中获取用户ID
     * 解析JWT令牌并提取其中的用户ID信息
     *
     * @param token JWT令牌
     * @return 用户ID，如果解析失败则返回null
     */
    public Long getUserIdFromToken(String token) {
        Long userId;
        try {
            Claims claims = getClaimsFromToken(token);
            userId = Long.valueOf(claims.get("userId").toString()); // 从自定义声明中获取用户ID
        } catch (Exception e) {
            log.error("从token中获取用户ID失败", e);
            userId = null;
        }
        return userId;
    }

    /**
     * 验证token是否有效
     * 检查令牌的用户名是否匹配、是否已过期
     *
     * @param token    JWT令牌
     * @param username 待验证的用户名
     * @return 如果令牌有效且匹配用户名则返回true，否则返回false
     */
    public boolean validateToken(String token, String username) {
        String tokenUsername = getUsernameFromToken(token);
        return tokenUsername != null && tokenUsername.equals(username) && !isTokenExpired(token);
    }

    /**
     * 判断token是否已经失效
     * 检查令牌的过期时间是否早于当前时间
     *
     * @param token JWT令牌
     * @return 如果令牌已过期则返回true，否则返回false
     */
    private boolean isTokenExpired(String token) {
        Date expiredDate = getExpiredDateFromToken(token);
        return expiredDate.before(new Date());
    }

    /**
     * 从token中获取过期时间
     * 解析JWT令牌并提取其中的过期时间信息
     *
     * @param token JWT令牌
     * @return 过期时间对象
     */
    private Date getExpiredDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration(); // exp是JWT标准声明，表示过期时间
    }

    /**
     * 从token中获取JWT的负载
     * 解析JWT令牌并提取其中的所有声明(Claims)
     *
     * @param token JWT令牌
     * @return 包含所有声明的Claims对象，如果解析失败则返回null
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) // 设置验证签名的密钥
                    .build()
                    .parseClaimsJws(token) // 解析令牌
                    .getBody(); // 获取声明部分
        } catch (Exception e) {
            log.error("JWT格式验证失败", e);
        }
        return claims;
    }

    /**
     * 根据负载生成JWT的token
     * 使用指定的声明创建一个带有过期时间和签名的JWT令牌
     *
     * @param claims 包含用户信息的声明映射
     * @return 生成的JWT令牌字符串
     */
    private String generateToken(Map<String, Object> claims) {
        Date createdTime = (Date) claims.get("created");
        Date expirationDate = new Date(createdTime.getTime() + expiration * 1000); // 计算过期时间
        return Jwts.builder()
                .setClaims(claims) // 设置自定义声明
                .setExpiration(expirationDate) // 设置过期时间
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // 使用HS512算法签名
                .compact(); // 生成最终的JWT字符串
    }

    /**
     * 从请求头中获取token
     * 从HTTP请求的Authorization头中提取JWT令牌
     *
     * @param header 请求头中的Authorization值
     * @return 提取的JWT令牌，如果格式不正确则返回null
     */
    public String getTokenFromHeader(String header) {
        if (StrUtil.isBlank(header)) {
            return null;
        }
        // 检查是否以指定的令牌头(如"Bearer ")开头
        if (header.startsWith(tokenHead)) {
            // 去除令牌头部分，获取实际JWT字符串
            return header.substring(tokenHead.length()).trim();
        }
        return null;
    }

    /**
     * 获取token的剩余有效期（秒）
     * 计算JWT令牌的过期时间与当前时间的差值
     *
     * @param token JWT令牌
     * @return 剩余有效期（秒）
     */
    public long getTokenRemainingTime(String token) {
        Date expiredDate = getExpiredDateFromToken(token);
        return (expiredDate.getTime() - DateUtil.date().getTime()) / 1000;
    }
} 