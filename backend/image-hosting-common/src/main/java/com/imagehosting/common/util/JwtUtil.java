package com.imagehosting.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

/**
 * JWT工具类
 */
@Data
@Component
public class JwtUtil {

    /**
     * 密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * 过期时间（毫秒）
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 请求头
     */
    @Value("${jwt.header}")
    private String header;

    /**
     * 令牌前缀
     */
    @Value("${jwt.tokenPrefix}")
    private String tokenPrefix;

    /**
     * 密钥对象
     */
    private Key key;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);//使用HS512算法生成密钥
    }

    /**
     * 生成令牌
     *
     * @param userId 用户ID
     * @return 令牌
     */
    public String generateToken(String userId) {
        //generateToken 方法的主要功能是根据传入的用户 ID 生成一个带有签发时间
        //过期时间的 JWT 字符串，使用 HS512 算法进行签名。
        Date now = new Date();//now里存放当前时间
        Date expiryDate = new Date(now.getTime() + expiration);//expiryDate过期时间=当前时间+过期时间

        return Jwts.builder()
                .setSubject(userId)//传入用户ID
                .setIssuedAt(now)//设置签发时间
                .setExpiration(expiryDate)//设置过期时间
                .signWith(key, SignatureAlgorithm.HS512)//使用HS512算法进行签名
                .compact();//生成令牌
    }

    /**
     * 验证令牌
     *
     * @param token 令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        //先解析令牌，如果解析成功，则返回true，否则返回false
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()//构建解析器
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token 令牌
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {//从令牌中获取用户ID
        Claims claims = Jwts.parserBuilder()//构建解析器
                .setSigningKey(key)//设置签名密钥
                .build()//构建解析器
                .parseClaimsJws(token)//解析令牌
                .getBody();//获取令牌主体
                
        return claims.getSubject();//返回用户ID
    }
} 