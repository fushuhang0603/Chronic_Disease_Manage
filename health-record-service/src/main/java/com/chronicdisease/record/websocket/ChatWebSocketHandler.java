package com.chronicdisease.record.websocket;

import com.chronicdisease.record.domain.dto.ConsultationMessageDTO;
import com.chronicdisease.record.domain.entity.ConsultationRecord;
import com.chronicdisease.record.service.IConsultationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 医患聊天 WebSocket 核心处理器
 */
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    /** 在线用户: userId -> WebSocketSession */
    private static final Map<Long, WebSocketSession> ONLINE_SESSIONS = new ConcurrentHashMap<>();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private IConsultationService consultationService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 用户建立 WebSocket 连接时触发
     * 作用：记录用户在线状态 + 推送未读消息红点数
     * 1. 从拦截器存入的 session 属性中取出 userId 和 role
     * 2. 存入内存在线表（ConcurrentHashMap），用于快速查找连接发送消息
     * 3. 写入 Redis 在线标记（TTL 60s），用于心跳保活和跨实例判断在线
     * 4. 从 Redis 读取该用户的未读消息计数，推送给前端显示红点
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        String role = (String) session.getAttributes().get("role");

        if (userId == null) {
            log.warn("WebSocket 连接建立失败: userId 为空");
            try { session.close(); } catch (Exception ignored) {}
            return;
        }

        // 存入在线表
        ONLINE_SESSIONS.put(userId, session);

        // 写入 Redis 在线状态，60 秒 TTL
        redisTemplate.opsForValue().set("online:" + userId, "1", java.time.Duration.ofSeconds(60));

        // 推送未读消息计数给刚上线的用户
        Map<Object, Object> unreadMap = redisTemplate.opsForHash().entries("unread:" + userId);
        if (!unreadMap.isEmpty()) {
            try {
                // 返回格式: { type: "unread_count", data: { patientId: count, ... } }
                Map<String, Object> unreadMsg = Map.of(
                        "type", "unread_count",
                        "data", unreadMap
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(unreadMsg)));
            } catch (Exception e) {
                log.error("推送未读计数失败", e);
            }
        }

        log.info("WebSocket 连接建立: userId={}, role={}, 当前在线数={}", userId, role, ONLINE_SESSIONS.size());
    }

    /**
     * 收到客户端发来的文本消息时触发
     * 完整流程：解析消息JSON → 校验发送者身份 → Feign查姓名 → 存MySQL →
     *           更新Redis未读计数 → WebSocket推送给接收方
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long userId = (Long) session.getAttributes().get("userId");
        String role = (String) session.getAttributes().get("role");

        if (userId == null || role == null) {
            log.warn("收到消息但 session 中无身份信息");
            return;
        }

        try {
            //  解析 JSON → ConsultationMessageDTO
            ConsultationMessageDTO dto = objectMapper.readValue(message.getPayload(), ConsultationMessageDTO.class);

            //  调用 Service 发送消息（存库 + Redis + 推送）
            ConsultationRecord record = consultationService.sendMessage(userId, role, dto);

            //  给发送方回执确认
            Map<String, Object> ack = Map.of(
                    "type", "ack",
                    "messageId", String.valueOf(record.getId()),
                    "createTime", record.getCreateTime().toString()
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));

        } catch (Exception e) {
            log.error("处理消息失败: userId={}, role={}", userId, role, e);
            try {
                session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"消息发送失败\"}"));
            } catch (Exception ignored) {}
        }
    }

    /**
     * 用户正常关闭 WebSocket 连接时触发（关闭页面、退出登录等）
     * 作用：清理用户在线状态，防止对端误以为还在线
     * 1. 从内存在线表中移除
     * 2. 删除 Redis 在线标记
     * 注意：不会删除未读计数 Redis 缓存，下次上线仍能看到红点
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            ONLINE_SESSIONS.remove(userId);
            redisTemplate.delete("online:" + userId);
            log.info("WebSocket 连接关闭: userId={}, 当前在线数={}", userId, ONLINE_SESSIONS.size());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket 传输错误: {}", session.getId(), exception);
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            ONLINE_SESSIONS.remove(userId);
            redisTemplate.delete("online:" + userId);
        }
    }

    /** 获取在线用户的 session */
    public static WebSocketSession getOnlineSession(Long userId) {
        return ONLINE_SESSIONS.get(userId);
    }

    /**
     * 判断用户当前是否在线（连接存在且未关闭）
     * 用于发消息前判断是否需要 WebSocket 推送，还是只存库等下次上线加载
     */
    public static boolean isOnline(Long userId) {
        return ONLINE_SESSIONS.containsKey(userId)
                && ONLINE_SESSIONS.get(userId).isOpen();
    }
}
