package com.chronicdisease.record.websocket;

import com.chronicdisease.common.util.JwtTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 * 从 JWT token 中解析 userId 和 role，存入 session 属性
 */
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // 从请求参数中获取 token（WebSocket URL 参数方式）
            String token = servletRequest.getServletRequest().getParameter("token");

            // 参数没有则尝试从 Authorization 请求头获取
            if (StringUtils.isBlank(token)) {
                String authHeader = servletRequest.getServletRequest().getHeader("Authorization");
                if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            if (StringUtils.isBlank(token)) {
                log.warn("WebSocket 握手失败: token 为空");
                return false;
            }

            try {
                Map<String, Object> claims = JwtTool.parseToken(token);
                Long userId = (Long) claims.get("userId");
                String role = (String) claims.get("role");

                if (userId != null) {
                    attributes.put("userId", userId);
                }
                if (role != null) {
                    attributes.put("role", role);
                }
                log.info("WebSocket 握手成功: userId={}, role={}", userId, role);
                return true;
            } catch (Exception e) {
                log.warn("WebSocket 握手失败: JWT 解析异常", e);
                return false;
            }
        }
        log.warn("WebSocket 握手失败: 当前运行环境非 Servlet 容器");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.warn("WebSocket 握手异常: {}", exception.getMessage());
        }
    }
}
