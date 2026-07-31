package com.chronicdisease.record.websocket;

import com.chronicdisease.common.util.JwtTool;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 握手拦截器：从 URL 参数中提取 JWT，校验后注入用户信息到会话属性
 */
@Slf4j
public class ChatHandshakeConfigurator extends ServerEndpointConfig.Configurator {

    private static final String TOKEN_PARAM = "token";

    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        // 从 URL 查询参数中获取 token: ws://host/ws/chat?token=xxx
        List<String> tokens = request.getParameterMap().get(TOKEN_PARAM);
        if (tokens == null || tokens.isEmpty()) {
            log.warn("WebSocket握手失败：缺少 token 参数");
            return;
        }

        String token = tokens.get(0);
        Map<String, Object> userInfo;
        try {
            userInfo = JwtTool.parseToken(token);
        } catch (Exception e) {
            log.warn("WebSocket握手失败：token 无效, token={}", token, e);
            return;
        }

        Long userId = (Long) userInfo.get("userId");
        String role = (String) userInfo.get("role");
        if (userId == null) {
            log.warn("WebSocket握手失败：token 中缺少 userId");
            return;
        }

        // 将用户信息注入 userProperties，@OnOpen 中通过 config.getUserProperties() 获取
        Map<String, Object> props = config.getUserProperties();
        props.put("userId", userId);
        props.put("role", role);
        log.info("WebSocket握手成功：userId={}, role={}", userId, role);
    }
}
