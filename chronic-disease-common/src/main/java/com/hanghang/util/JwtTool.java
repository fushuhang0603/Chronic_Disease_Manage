package com.hanghang.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
public class JwtTool {

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

    public static Map<String, Object> parseToken(String token) {
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Map<String, Object> result = new HashMap<>();
            result.put("userId", claims.get("userId", Long.class));
            result.put("role", claims.get("role", String.class));
            return result;
    }

    /**
     * 获取用户id
     * @param token
     * @return
     */
    public static Long getUserId(String token) {
        Map<String, Object> claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return (Long) claims.get("userId");
    }

    /**
     * 获取用户角色
     * @param token
     * @return
     */
    public static String getRole(String token) {
        Map<String, Object> claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return (String) claims.get("role");
    }
}
