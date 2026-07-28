package com.chronicdisease.record.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.record.domain.dto.ConsultationMessageDTO;
import com.chronicdisease.record.domain.dto.ConsultationPageDTO;
import com.chronicdisease.record.domain.entity.ConsultationRecord;
import com.chronicdisease.record.domain.vo.ConsultationMessageVO;
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

        // WebSocket 推送（接收方 + 发送方都推，双方实时看到消息）
        pushToUser(receiverId, senderId, senderRole, dto, record);
        pushToUser(senderId, senderId, senderRole, dto, record);

        return record;
    }

    @Override
    public LinkedHashMap<String, List<ConsultationMessageVO>> pageHistory(ConsultationPageDTO dto) {
        LambdaQueryWrapper<ConsultationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationRecord::getPatientId, dto.getPatientId())
                .eq(ConsultationRecord::getDoctorId, dto.getDoctorId())
                .eq(ConsultationRecord::getIsDeleted, BusinessConstant.isNotDelete)
                .orderByAsc(ConsultationRecord::getCreateTime);

        List<ConsultationRecord> records = consultationRecordMapper.selectList(wrapper);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LinkedHashMap<String, List<ConsultationMessageVO>> result = new LinkedHashMap<>();
        for (ConsultationRecord record : records) {
            String dayKey = record.getCreateTime().format(dayFormatter);
            ConsultationMessageVO vo = new ConsultationMessageVO();
            vo.setSenderId(record.getSenderId());
            vo.setSenderRole(record.getSenderRole());
            vo.setContent(record.getContent());
            vo.setCreateTime(record.getCreateTime().format(timeFormatter));
            //用创建时间具体到某一天创建key,再new一个集合出来,把符合时间的记录加入到集合
            result.computeIfAbsent(dayKey, k -> new ArrayList<>()).add(vo);
        }
        return result;
    }

    @Override
    public void markRead(Long targetId) {
        Long userId = UserInfoContext.getUserId();
        String role = UserInfoContext.getRole();
        // 根据角色确定患者和医生
        Long patientId, doctorId;
        if ("PATIENT".equalsIgnoreCase(role)) {
            patientId = userId;
            doctorId = targetId;
        } else {
            patientId = targetId;
            doctorId = userId;
        }
        // 对方角色
        String otherRole = "PATIENT".equalsIgnoreCase(role) ? "DOCTOR" : "PATIENT";

        // 更新 MySQL：把对方发给我的未读消息标记为已读
        LambdaUpdateWrapper<ConsultationRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ConsultationRecord::getPatientId, patientId)
                .eq(ConsultationRecord::getDoctorId, doctorId)
                .eq(ConsultationRecord::getSenderRole, otherRole)
                .eq(ConsultationRecord::getIsRead, 0)
                .set(ConsultationRecord::getIsRead, 1);
        consultationRecordMapper.update(null, wrapper);

        // 清 Redis 未读计数
        redisTemplate.opsForHash().delete("unread:" + userId, String.valueOf(targetId));

        log.info("标记已读完成: userId={}, role={}, targetId={}", userId, role, targetId);
    }

    @Override
    public LinkedHashMap<String, DoctorPatientVO> getDoctorPatients(Long doctorId) {
        //查询与医生绑定的患者信息
        List<Long> patientIds = userServiceFeign.getMyPatients(doctorId).getData();
        if (patientIds == null || patientIds.isEmpty()){
            log.info("医生{}没有任何所绑定的患者", doctorId);
            return new LinkedHashMap<>();
        }
        //查询该医生和绑定患者的最新交流记录
        List<ConsultationRecord> data = consultationRecordMapper.getRecords(doctorId,patientIds);
        if (data == null || data.isEmpty()){
            log.info("医生{}和患者之间没有任何咨询记录", doctorId);
            return new LinkedHashMap<>();
        }
        //处理交流记录
        Map<Object, Object> unreadMap = redisTemplate.opsForHash()
                .entries("unread:" + doctorId);
        LinkedHashMap<String, DoctorPatientVO> resultMap = new LinkedHashMap<>();
        Map<Long, List<ConsultationRecord>> map = data.stream().collect(Collectors.groupingBy(ConsultationRecord::getPatientId));
        map.forEach((patientId, records) -> {
            DoctorPatientVO vo = new DoctorPatientVO();
            vo.setPatientId(patientId);
            vo.setPatientName(records.get(0).getPatientName());
            vo.setOnline(ChatWebSocketHandler.isOnline(patientId));
            // 未读数（Redis）
            Object unreadObj = unreadMap.get(String.valueOf(patientId));
            vo.setUnreadCount(unreadObj != null ? Integer.parseInt(unreadObj.toString()) : 0);
            // 最新消息（SQL 已按时间 desc，第一条最新）
            ConsultationRecord latest = records.get(0);
            String content = latest.getContent();
            vo.setLatestContent(content.length() > 30 ? content.substring(0, 30) + "..." : content);
            vo.setLatestTime(latest.getCreateTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
            resultMap.put(String.valueOf(patientId), vo);
        });
        // 兜底：绑定了但没有聊天记录的患者也展示
        for (Long patientId : patientIds) {
            resultMap.putIfAbsent(String.valueOf(patientId), emptyVO(patientId, unreadMap));
        }
        return resultMap;
    }

    private DoctorPatientVO emptyVO(Long patientId, Map<Object, Object> unreadMap) {
        DoctorPatientVO vo = new DoctorPatientVO();
        vo.setPatientId(patientId);
        vo.setPatientName(fetchNickname(patientId));
        vo.setOnline(ChatWebSocketHandler.isOnline(patientId));
        Object unreadObj = unreadMap.get(String.valueOf(patientId));
        vo.setUnreadCount(unreadObj != null ? Integer.parseInt(unreadObj.toString()) : 0);
        return vo;
    }



    /**
     * WebSocket 推送消息给指定用户
     */
    private void pushToUser(Long targetUserId, Long senderId, String senderRole,
                            ConsultationMessageDTO dto, ConsultationRecord record) {
        if (!ChatWebSocketHandler.isOnline(targetUserId)) {
            return;
        }
        try {
            WebSocketSession session = ChatWebSocketHandler.getOnlineSession(targetUserId);
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
                log.info("WebSocket 推送成功: targetUserId={}, senderId={}", targetUserId, senderId);
            }
        } catch (Exception e) {
            log.error("WebSocket 推送失败: targetUserId={}, senderId={}", targetUserId, senderId, e);
        }
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
