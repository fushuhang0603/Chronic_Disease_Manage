package com.hanghang.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 工具类
 */
public class JwtUtil {

    private static final SecretKey SECRET = Keys.hmacShaKeyFor(
            "chronic-disease-manage-2026-secret-key".getBytes()
    );
    private static final long EXPIRE_MS = 24L * 60 * 60 * 1000; // 24小时

    /**
     * 生成 JWT token
     */
    public static String createToken(Long userId, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + EXPIRE_MS))
                .signWith(SECRET, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 校验 token 签名和过期
     */
    public static boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 token 提取用户 ID
     */
    public static Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    /**
     * 从 token 提取用户角色
     */
    public static String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    private static Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
