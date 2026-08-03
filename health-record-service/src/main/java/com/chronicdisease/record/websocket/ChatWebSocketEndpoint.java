package com.chronicdisease.record.websocket;

import com.alibaba.fastjson2.JSON;
import com.chronicdisease.record.domain.entity.ChatMessage;
import com.chronicdisease.record.service.IConsultationRecordService;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ServerEndpoint(value = "/ws/chat", configurator = ChatHandshakeConfigurator.class)
public class ChatWebSocketEndpoint {

    // 在线用户会话池
    private static final ConcurrentHashMap<Long, Session> ONLINE_USER_SESSION = new ConcurrentHashMap<>();

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 离线消息 Redis 队列 key 前缀：chat:offline:{userId}
    private static final String OFFLINE_KEY_PREFIX = "chat:offline:";
    // 离线队列保留时间
    private static final long OFFLINE_EXPIRE_DAYS = 7;

    /**
     * 通过静态字段桥接 Spring Bean 注入，供 Jakarta 容器创建的实例使用
     * */
    private static IConsultationRecordService consultationRecordService;

    private static StringRedisTemplate redisTemplate;

    private static Executor chatExecutor;


    private Session session;
    private Long userId;
    private String role;

    @Autowired
    public void setConsultationRecordService(IConsultationRecordService service) {
        ChatWebSocketEndpoint.consultationRecordService = service;
    }

    @Autowired
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        ChatWebSocketEndpoint.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setChatExecutor(@Qualifier("chatExecutor") Executor executor) {
        ChatWebSocketEndpoint.chatExecutor = executor;
    }


    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        Map<String, Object> attr = config.getUserProperties();
        this.userId = Long.valueOf(attr.get("userId").toString());
        this.role = (String) attr.get("role");
        ONLINE_USER_SESSION.put(userId, session);
        log.info("用户上线: userId={}, 当前在线人数={}", userId, ONLINE_USER_SESSION.size());

        // 上线补偿：异步去 Redis 查离线期间的消息并推送给用户（不阻塞 WebSocket 容器线程）
        chatExecutor.execute(() -> pushOfflineMessages(this.userId));
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            ChatMessage incoming = JSON.parseObject(message, ChatMessage.class);
            Long toUserId = Long.valueOf(incoming.getToUserId());
            String content = incoming.getContent();

            if (toUserId == null || content == null || content.trim().isEmpty()) {
                sendError("消息格式错误：缺少接收人或消息内容");
                return;
            }

            // 构建消息：发送人信息由服务端填充,客户端不可信
            ChatMessage outgoing = new ChatMessage();
            outgoing.setFromUserId(String.valueOf(this.userId));
            outgoing.setToUserId(String.valueOf(toUserId));
            outgoing.setContent(content.trim());
            outgoing.setTime(LocalDateTime.now().format(TIME_FMT));
            outgoing.setSenderName(incoming.getSenderName());
            outgoing.setSenderRole(incoming.getSenderRole());

            String outgoingJson = JSON.toJSONString(outgoing);

            // 推送给接收方
            Session targetSession = ONLINE_USER_SESSION.get(toUserId);
            if (targetSession != null && targetSession.isOpen()) {
                targetSession.getBasicRemote().sendText(outgoingJson);
                log.info("消息已推送: from={} to={}", this.userId, toUserId);
            } else {
                // 目标用户不在线：缓存到 Redis 待推队列，等对方上线后补偿推送（消息仍会异步入库）
                cacheOfflineMessage(String.valueOf(toUserId), outgoingJson);
            }

            // 异步入库
            Long patientId = "DOCTOR".equalsIgnoreCase(role) ? toUserId : this.userId;
            Long doctorId = "PATIENT".equalsIgnoreCase(role) ? toUserId : this.userId;
            consultationRecordService.saveMessage(patientId, incoming.getPatientName(),
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

    /**
     *接收端用户离线时,缓存离线消息
     * @param toUserId
     * @param message
     */
    public void cacheOfflineMessage(String toUserId, String message) {
        try {
            String key = OFFLINE_KEY_PREFIX+toUserId;
            redisTemplate.opsForList().rightPush(key,message);
            redisTemplate.expire(key,OFFLINE_EXPIRE_DAYS, TimeUnit.DAYS);
            log.info("目标离线，消息已入 Redis 待推队列: toUserId={}", toUserId);
        } catch (Exception e) {
            log.warn("缓存离线消息失败: toUserId={}, msg={}", toUserId, message, e);
        }
    }

    /**
     * 查询离线消息并推送给用户
     * 主链路查 Redis，Redis 异常时 MySQL 兜底
     * @param onlineUserId
     */
    private void pushOfflineMessages(Long onlineUserId) {
        // 判断用户是否还在线（异步任务执行时用户可能已下线）
        Session s = ONLINE_USER_SESSION.get(onlineUserId);
        if (s == null || !s.isOpen()) {
            return;
        }

        //推送后清空
        try {
            String key = OFFLINE_KEY_PREFIX + onlineUserId;
            if (redisTemplate.hasKey(key)) {
                List<String> offlineMessages = redisTemplate.opsForList().range(key, 0, -1);
                if (offlineMessages != null && !offlineMessages.isEmpty()) {
                    for (String json : offlineMessages) {
                        sendText(s, json);
                    }
                    redisTemplate.delete(key);
                    log.info("离线消息补偿推送完成: userId={}, 条数={}", onlineUserId, offlineMessages.size());
                    return;
                }
            }
            log.info("Redis 离线队列为空: userId={}", onlineUserId);
        } catch (Exception e) {
            // Redis 不可用时降级,不影响消息推送
            log.warn("读取 Redis 离线队列失败，回退 DB 兜底: userId={}, err={}", onlineUserId, e.getMessage());
        }

        // 兜底链路：DB 查发给当前用户,尚未读的消息
        try {
            List<ChatMessage> dbMessages = consultationRecordService.queryOfflineUnreadMessages(onlineUserId, this.role);
            for (ChatMessage msg : dbMessages) {
                sendText(s, JSON.toJSONString(msg));
            }
            log.info("DB 兜底推送离线消息完成: userId={}, 条数={}", onlineUserId, dbMessages.size());
        } catch (Exception e) {
            log.error("DB 兜底推送离线消息失败: userId={}", onlineUserId, e);
        }
    }

    /**
     * 线程安全地向指定连接发送文本：synchronized 防同一连接并发写，isOpen 兜底防已断开连接
     */
    private void sendText(Session target, String text) {
        try {
            synchronized (target) {
                if (target.isOpen()) {
                    target.getBasicRemote().sendText(text);
                }
            }
        } catch (IOException e) {
            log.warn("发送消息失败: sessionId={}, err={}", target.getId(), e.getMessage());
        }
    }
}
