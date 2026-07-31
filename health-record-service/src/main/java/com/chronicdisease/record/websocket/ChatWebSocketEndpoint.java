package com.chronicdisease.record.websocket;

import com.alibaba.fastjson2.JSON;
import com.chronicdisease.record.domain.entity.ChatMessage;
import com.chronicdisease.record.util.SpringContextUtil;
import com.chronicdisease.record.service.IConsultationRecordService;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint(value = "/ws/chat", configurator = ChatHandshakeConfigurator.class)
public class ChatWebSocketEndpoint {

    /** 在线用户会话池：userId -> Session */
    private static final ConcurrentHashMap<Long, Session> ONLINE_USER_SESSION = new ConcurrentHashMap<>();

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Session session;
    private Long userId;
    private String role;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        Map<String, Object> attr = config.getUserProperties();
        this.userId = Long.valueOf(attr.get("userId").toString());
        this.role = (String) attr.get("role");
        ONLINE_USER_SESSION.put(userId, session);
        log.info("用户上线: userId={}, 当前在线人数={}", userId, ONLINE_USER_SESSION.size());
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            ChatMessage incoming = JSON.parseObject(message, ChatMessage.class);
            Long toUserId = incoming.getToUserId();
            String content = incoming.getContent();

            if (toUserId == null || content == null || content.trim().isEmpty()) {
                sendError("消息格式错误：缺少接收人或消息内容");
                return;
            }

            // 构建消息：发送人信息由服务端填充，客户端不可信
            ChatMessage outgoing = new ChatMessage();
            outgoing.setFromUserId(this.userId);
            outgoing.setToUserId(toUserId);
            outgoing.setContent(content.trim());
            outgoing.setTime(LocalDateTime.now().format(TIME_FMT));
            outgoing.setSenderName(incoming.getSenderName());
            outgoing.setSenderRole(incoming.getSenderRole());

            String outgoingJson = JSON.toJSONString(outgoing);

            // 推送给接收方
            Session targetSession = ONLINE_USER_SESSION.get(toUserId);
            if (targetSession != null && targetSession.isOpen()) {
                targetSession.getBasicRemote().sendText(outgoingJson);
            }

            // 异步入库
            IConsultationRecordService service = SpringContextUtil.getBean(IConsultationRecordService.class);
            Long patientId = "DOCTOR".equals(role) ? toUserId : this.userId;
            Long doctorId = "PATIENT".equals(role) ? toUserId : this.userId;
            service.saveMessage(patientId, incoming.getPatientName(),
                    doctorId, incoming.getDoctorName(),
                    this.userId, this.role, content.trim());

        } catch (Exception e) {
            log.error("消息处理失败: userId={}, msg={}", this.userId, message, e);
            sendError("消息处理失败: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose() {
        if (userId != null) {
            ONLINE_USER_SESSION.remove(userId);
            log.info("用户下线: userId={}, 当前在线人数={}", userId, ONLINE_USER_SESSION.size());
        }
    }

    @OnError
    public void onError(Throwable error) {
        log.error("WebSocket异常: userId={}, sessionId={}", userId, session != null ? session.getId() : "null", error);
        if (userId != null) {
            ONLINE_USER_SESSION.remove(userId);
        }
    }

    /** 向当前连接发送错误消息 */
    private void sendError(String msg) {
        try {
            if (session != null && session.isOpen()) {
                String errorJson = String.format("{\"type\":\"error\",\"message\":\"%s\"}", msg);
                synchronized (session) {
                    session.getBasicRemote().sendText(errorJson);
                }
            }
        } catch (IOException ignored) {
            // 发送失败忽略
        }
    }

    /** 获取在线用户数（供外部监控） */
    public static int getOnlineCount() {
        return ONLINE_USER_SESSION.size();
    }
}
