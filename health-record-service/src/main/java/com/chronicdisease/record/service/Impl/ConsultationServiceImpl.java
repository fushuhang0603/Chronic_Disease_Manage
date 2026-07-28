package com.chronicdisease.record.service.Impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.record.domain.dto.ConsultationMessageDTO;
import com.chronicdisease.record.domain.dto.ConsultationPageDTO;
import com.chronicdisease.record.domain.entity.ConsultationRecord;
import com.chronicdisease.record.domain.vo.DoctorPatientVO;
import com.chronicdisease.record.domain.vo.UserBriefVO;
import com.chronicdisease.record.feign.UserServiceFeign;
import com.chronicdisease.record.mapper.ConsultationRecordMapper;
import com.chronicdisease.record.service.IConsultationService;
import com.chronicdisease.record.websocket.ChatWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConsultationServiceImpl extends ServiceImpl<ConsultationRecordMapper, ConsultationRecord>
        implements IConsultationService {

    @Autowired
    private ConsultationRecordMapper consultationRecordMapper;

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ConsultationRecord sendMessage(Long senderId, String senderRole, ConsultationMessageDTO dto) {
        // 校验发送者身份：患者只能以自己身份发，医生只能以自己身份发
        validateSender(senderId, senderRole, dto);

        // 通过 Feign 查姓名
        String patientName = fetchNickname(dto.getPatientId());
        String doctorName = fetchNickname(dto.getDoctorId());

        // 确定接收方
        Long receiverId = senderRole.equals("PATIENT") ? dto.getDoctorId() : dto.getPatientId();

        // 存 MySQL
        ConsultationRecord record = new ConsultationRecord();
        record.setPatientId(dto.getPatientId());
        record.setPatientName(patientName);
        record.setDoctorId(dto.getDoctorId());
        record.setDoctorName(doctorName);
        record.setSenderId(senderId);
        record.setSenderRole(senderRole);
        record.setContent(dto.getContent());
        record.setIsRead(0);
        record.setIsDeleted(BusinessConstant.isNotDelete);
        consultationRecordMapper.insert(record);

        // Redis 未读计数 +1
        String unreadKey = "unread:" + receiverId;
        String senderIdKey = String.valueOf(senderId);
        redisTemplate.opsForHash().increment(unreadKey, senderIdKey, 1);

        // WebSocket 推送（接收方在线才推）
        if (ChatWebSocketHandler.isOnline(receiverId)) {
            try {
                WebSocketSession session = ChatWebSocketHandler.getOnlineSession(receiverId);
                if (session != null && session.isOpen()) {
                    Map<String, Object> pushMsg = new LinkedHashMap<>();
                    pushMsg.put("type", "new_message");
                    pushMsg.put("senderId", String.valueOf(senderId));
                    pushMsg.put("senderRole", senderRole);
                    pushMsg.put("content", dto.getContent());
                    pushMsg.put("patientId", String.valueOf(dto.getPatientId()));
                    pushMsg.put("doctorId", String.valueOf(dto.getDoctorId()));
                    pushMsg.put("createTime", record.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pushMsg)));
                    log.info("WebSocket 推送成功: senderId={}, receiverId={}", senderId, receiverId);
                }
            } catch (Exception e) {
                log.error("WebSocket 推送失败: senderId={}, receiverId={}", senderId, receiverId, e);
            }
        }

        return record;
    }

    @Override
    public PageResult<ConsultationRecord> pageHistory(ConsultationPageDTO dto) {
        LambdaQueryWrapper<ConsultationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationRecord::getPatientId, Long.valueOf(dto.getPatientId()))
                .eq(ConsultationRecord::getDoctorId, Long.valueOf(dto.getDoctorId()))
                .eq(ConsultationRecord::getIsDeleted, BusinessConstant.isNotDelete)
                .orderByAsc(ConsultationRecord::getCreateTime);

        Page<ConsultationRecord> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<ConsultationRecord> result = consultationRecordMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public void markRead(Long readerId, Long patientId, Long doctorId) {
        // 确定当前用户的角色（判断读者是患者还是医生）
        String readerRole = readerId.equals(patientId) ? "PATIENT" : "DOCTOR";
        // 对方角色
        String otherRole = "PATIENT".equals(readerRole) ? "DOCTOR" : "PATIENT";

        // 更新 MySQL：把对方发给我的未读消息标记为已读
        LambdaUpdateWrapper<ConsultationRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ConsultationRecord::getPatientId, patientId)
                .eq(ConsultationRecord::getDoctorId, doctorId)
                .eq(ConsultationRecord::getSenderRole, otherRole)
                .eq(ConsultationRecord::getIsRead, 0)
                .set(ConsultationRecord::getIsRead, 1);
        consultationRecordMapper.update(null, wrapper);

        // 清 Redis 未读计数
        String unreadKey = "unread:" + readerId;
        Long otherId = "PATIENT".equals(readerRole) ? doctorId : patientId;
        redisTemplate.opsForHash().delete(unreadKey, String.valueOf(otherId));

        log.info("标记已读完成: readerId={}, patientId={}, doctorId={}", readerId, patientId, doctorId);
    }

    @Override
    public List<DoctorPatientVO> getDoctorPatients(Long doctorId) {
        // Feign 获取该医生的所有绑定患者ID
        List<Long> patientIds;
        try {
            patientIds = userServiceFeign.getMyPatients(doctorId).getData();
        } catch (Exception e) {
            log.error("Feign 获取医生患者列表失败: doctorId={}", doctorId, e);
            return Collections.emptyList();
        }
        if (CollUtil.isEmpty(patientIds)) {
            return Collections.emptyList();
        }

        // 查 Redis 未读数
        String unreadKey = "unread:" + doctorId;
        Map<Object, Object> unreadMap = redisTemplate.opsForHash().entries(unreadKey);

        // 组装结果
        return patientIds.stream().map(patientId -> {
            DoctorPatientVO vo = new DoctorPatientVO();
            vo.setPatientId(patientId);
            vo.setPatientName(fetchNickname(patientId));
            vo.setOnline(ChatWebSocketHandler.isOnline(patientId));

            // 未读数
            Object unreadObj = unreadMap.get(String.valueOf(patientId));
            vo.setUnreadCount(unreadObj != null ? Integer.parseInt(unreadObj.toString()) : 0);

            // 最新一条消息
            LambdaQueryWrapper<ConsultationRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConsultationRecord::getPatientId, patientId)
                    .eq(ConsultationRecord::getDoctorId, doctorId)
                    .eq(ConsultationRecord::getIsDeleted, BusinessConstant.isNotDelete)
                    .orderByDesc(ConsultationRecord::getCreateTime)
                    .last("LIMIT 1");
            ConsultationRecord latest = consultationRecordMapper.selectOne(wrapper);
            if (latest != null) {
                // 内容过长截断
                String content = latest.getContent();
                vo.setLatestContent(content.length() > 30 ? content.substring(0, 30) + "..." : content);
                vo.setLatestTime(latest.getCreateTime() != null
                        ? latest.getCreateTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
                        : "");
            }
            return vo;
        }).collect(Collectors.toList());
    }
    

    /**
     * 校验发送者身份合法性
     */
    private void validateSender(Long senderId, String senderRole, ConsultationMessageDTO dto) {
        if ("PATIENT".equalsIgnoreCase(senderRole)) {
            if (!senderId.equals(dto.getPatientId())) {
                throw new BusinessException("只能以自己的身份发送消息");
            }
        } else if ("DOCTOR".equalsIgnoreCase(senderRole)) {
            if (!senderId.equals(dto.getDoctorId())) {
                throw new BusinessException("只能以自己的身份发送消息");
            }
        } else {
            throw new BusinessException("无效的用户角色: " + senderRole);
        }
    }

    /**
     * Feign 查用户昵称，失败兜底返回 "用户{id}"
     */
    private String fetchNickname(Long userId) {
        try {
            Result<UserBriefVO> result = userServiceFeign.queryById(userId);
            if (result != null && result.getData() != null) {
                return result.getData().getNickname();
            }
        } catch (Exception e) {
            log.warn("Feign 查询用户昵称失败: userId={}", userId, e);
        }
        return "用户" + userId;
    }
}
