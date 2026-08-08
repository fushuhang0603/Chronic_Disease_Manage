package com.chronicdisease.record.mq;

import com.alibaba.fastjson2.JSON;
import com.chronicdisease.record.config.RabbitMqConfig;
import com.chronicdisease.record.domain.message.IndexAbnormalMessage;
import com.chronicdisease.record.domain.vo.DoctorBriefVO;
import com.chronicdisease.record.feign.UserServiceFeign;
import com.chronicdisease.record.websocket.ChatWebSocketEndpoint;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 指标异常消息消费者
 * 收到消息后查询绑定医生，在线则 WebSocket 推送，离线则缓存到 Redis
 */
@Slf4j
@Component
public class IndexAbnormalConsumer {

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 离线预警 Redis 队列 key 前缀：index:abnormal:offline:{doctorId}
     */
    private static final String OFFLINE_KEY_PREFIX = "index:abnormal:offline:";

    /**
     * 离线队列保留时间（天）
     */
    private static final long OFFLINE_EXPIRE_DAYS = 7;

    @RabbitListener(queues = RabbitMqConfig.queueName)
    public void handleAbnormalIndex(IndexAbnormalMessage message) {
        log.info("收到指标异常消息: patientId={}, indexCode={}, label={}",
                message.getPatientId(), message.getIndexCode(), message.getAbnormalLabel());

        try {
            // 1. 调用 health-user-service 查询绑定医生
            DoctorBriefVO doctor = userServiceFeign.getDoctorByPatientId(message.getPatientId()).getData();
            if (doctor == null || doctor.getDoctorId() == null) {
                log.info("患者未绑定医生，跳过预警推送: patientId={}", message.getPatientId());
                return;
            }

            // 2. 构建推送消息
            String notifyJson = buildNotifyMessage(message, doctor);

            // 3. 判断医生是否在线，在线推送 / 离线缓存
            Session doctorSession = ChatWebSocketEndpoint.getOnlineSession(doctor.getDoctorId());
            if (doctorSession != null && doctorSession.isOpen()) {
                synchronized (doctorSession) {
                    doctorSession.getBasicRemote().sendText(notifyJson);
                }
                log.info("预警已推送给医生: doctorId={}, patientId={}", doctor.getDoctorId(), message.getPatientId());
            } else {
                cacheOfflineMessage(doctor.getDoctorId(), notifyJson);
                log.info("医生离线，预警已缓存: doctorId={}, patientId={}", doctor.getDoctorId(), message.getPatientId());
            }
        } catch (Exception e) {
            log.error("处理指标异常消息失败: patientId={}", message.getPatientId(), e);
        }
    }

    /**
     * 构建 WebSocket 推送的预警通知 JSON
     */
    private String buildNotifyMessage(IndexAbnormalMessage msg, DoctorBriefVO doctor) {
        String patientName = msg.getPatientName() != null ? msg.getPatientName()
                : (doctor.getPatientName() != null ? doctor.getPatientName() : String.valueOf(msg.getPatientId()));
        String indexName = msg.getIndexName() != null ? msg.getIndexName() : msg.getIndexCode();
        return JSON.toJSONString(new java.util.LinkedHashMap<String, Object>() {{
            put("type", "index_abnormal");
            put("patientId", msg.getPatientId());
            put("patientName", patientName);
            put("doctorId", doctor.getDoctorId());
            put("doctorName", doctor.getDoctorName());
            put("indexCode", msg.getIndexCode());
            put("indexName", indexName);
            put("indexValue", msg.getIndexValue());
            put("unit", msg.getUnit());
            put("abnormalLabel", msg.getAbnormalLabel());
            put("recordId", msg.getRecordId());
            put("recordTime", msg.getRecordTime() != null ? msg.getRecordTime().format(TIME_FMT) : null);
            put("notifyTime", LocalDateTime.now().format(TIME_FMT));
            put("title", String.format("%s 指标异常（%s）", patientName, msg.getAbnormalLabel()));
            put("content", String.format("患者 %s 的 %s 检测结果为 %s %s，超出正常范围，请及时关注并联系患者。",
                    patientName, indexName, msg.getIndexValue(), msg.getUnit()));
        }});
    }

    /**
     * 医生离线时，将预警消息缓存到 Redis
     */
    private void cacheOfflineMessage(Long doctorId, String message) {
        try {
            String key = OFFLINE_KEY_PREFIX + doctorId;
            redisTemplate.opsForList().rightPush(key, message);
            redisTemplate.expire(key, OFFLINE_EXPIRE_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("缓存离线预警消息失败: doctorId={}", doctorId, e);
        }
    }
}
