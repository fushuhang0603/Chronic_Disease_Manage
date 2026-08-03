package com.chronicdisease.record.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.record.domain.dto.ConsultationPageDTO;
import com.chronicdisease.record.domain.entity.ChatMessage;
import com.chronicdisease.record.domain.entity.ConsultationRecord;
import com.chronicdisease.record.domain.vo.ChatRecordVO;
import com.chronicdisease.record.mapper.ConsultationRecordMapper;
import com.chronicdisease.record.service.IConsultationRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConsultationRecordServiceImpl extends ServiceImpl<ConsultationRecordMapper, ConsultationRecord>
        implements IConsultationRecordService {
    @Autowired
    private ConsultationRecordMapper consultationRecordMapper;

    @Async("chatExecutor")
    @Override
    public void saveMessage(Long patientId, String patientName,
                            Long doctorId, String doctorName,
                            Long senderId, String senderRole,
                            String content) {
        ConsultationRecord record = new ConsultationRecord();
        record.setPatientId(patientId);
        record.setPatientName(patientName);
        record.setDoctorId(doctorId);
        record.setDoctorName(doctorName);
        record.setSenderId(senderId);
        record.setSenderRole(senderRole);
        record.setContent(content);
        record.setIsRead(0);
        record.setIsDeleted(0);
        record.setCreateUser(senderId);
        consultationRecordMapper.insert(record);
    }

    @Override
    public PageResult<ChatRecordVO> pageRecords(ConsultationPageDTO dto) {
        Long currentUserId = UserInfoContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException("未获取到当前用户信息");
        }
        if (dto.getOtherUserId() == null) {
            throw new BusinessException("缺少对端用户ID");
        }
        String role = UserInfoContext.getRole();
        boolean isPatient = "PATIENT".equalsIgnoreCase(role);

        LambdaQueryWrapper<ConsultationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationRecord::getIsDeleted, BusinessConstant.isNotDelete);
        wrapper.orderByDesc(ConsultationRecord::getCreateTime);
        wrapper.orderByDesc(ConsultationRecord::getId);
        if(isPatient){
           wrapper.eq(ConsultationRecord::getPatientId, currentUserId)
                .eq(ConsultationRecord::getDoctorId, dto.getOtherUserId());
        }else{
            wrapper.eq(ConsultationRecord::getDoctorId, currentUserId)
                .eq(ConsultationRecord::getPatientId, dto.getOtherUserId());
        }

        //设置分页参数
        int pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        int pageSize = dto.getPageSize() == null ? 20 : dto.getPageSize();
        Page<ConsultationRecord> page = new Page<>(pageNum, pageSize);
        Page<ConsultationRecord> dbResult = consultationRecordMapper.selectPage(page, wrapper);

        List<ChatRecordVO> result = dbResult.getRecords().stream().map(
                record -> {
                    ChatRecordVO vo = new ChatRecordVO();
                    vo.setId(record.getId());
                    vo.setFromUserId(String.valueOf(record.getSenderId()));
                    if ("PATIENT".equalsIgnoreCase(record.getSenderRole())) {
                        vo.setSenderName(record.getPatientName());
                        vo.setToUserId(String.valueOf(record.getDoctorId()));
                    } else {
                        vo.setSenderName(record.getDoctorName());
                        vo.setToUserId(String.valueOf(record.getPatientId()));
                    }
                    vo.setContent(record.getContent());
                    vo.setTime(record.getCreateTime());
                    vo.setSenderRole(record.getSenderRole());
                    return vo;
                }
        ).collect(Collectors.toList());
        PageResult<ChatRecordVO> pageResult = new PageResult<>(result,dbResult.getTotal());
        return pageResult;
    }

    @Override
    public void markRead(Long otherUserId) {
        Long currentUserId = UserInfoContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException("未获取到当前用户信息");
        }
        if (otherUserId == null) {
            throw new BusinessException("缺少对端用户ID");
        }
        String role = UserInfoContext.getRole();
        boolean isPatient = "PATIENT".equalsIgnoreCase(role);

        LambdaQueryWrapper<ConsultationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationRecord::getIsDeleted, BusinessConstant.isNotDelete)
                .eq(ConsultationRecord::getSenderId, otherUserId)
                .eq(ConsultationRecord::getIsRead, 0);
        if (isPatient) {
            wrapper.eq(ConsultationRecord::getPatientId, currentUserId)
                    .eq(ConsultationRecord::getDoctorId, otherUserId);
        } else {
            wrapper.eq(ConsultationRecord::getDoctorId, currentUserId)
                    .eq(ConsultationRecord::getPatientId, otherUserId);
        }
        ConsultationRecord entity = new ConsultationRecord();
        entity.setIsRead(1);
        consultationRecordMapper.update(entity, wrapper);
        log.info("会话已读标记完成, currentUserId={}, otherUserId={}", currentUserId, otherUserId);
    }

    @Override
    public List<ChatMessage> queryOfflineUnreadMessages(Long userId, String role) {
        boolean isPatient = "PATIENT".equalsIgnoreCase(role);

        LambdaQueryWrapper<ConsultationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationRecord::getIsDeleted, BusinessConstant.isNotDelete)
                .eq(ConsultationRecord::getIsRead, 0)
                .ne(ConsultationRecord::getSenderId, userId)
                .orderByAsc(ConsultationRecord::getCreateTime);
        if (isPatient) {
            wrapper.eq(ConsultationRecord::getPatientId, userId);
        } else {
            wrapper.eq(ConsultationRecord::getDoctorId, userId);
        }
        List<ConsultationRecord> records = consultationRecordMapper.selectList(wrapper);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return records.stream().map(record -> {
            ChatMessage msg = new ChatMessage();
            msg.setFromUserId(String.valueOf(record.getSenderId()));
            msg.setToUserId(String.valueOf(isPatient ? record.getDoctorId() : record.getPatientId()));
            msg.setPatientId(String.valueOf(record.getPatientId()));
            msg.setPatientName(record.getPatientName());
            msg.setDoctorId(String.valueOf(record.getDoctorId()));
            msg.setDoctorName(record.getDoctorName());
            msg.setContent(record.getContent());
            msg.setTime(record.getCreateTime() == null ? null : record.getCreateTime().format(fmt));
            if ("PATIENT".equalsIgnoreCase(record.getSenderRole())) {
                msg.setSenderName(record.getPatientName());
            } else {
                msg.setSenderName(record.getDoctorName());
            }
            msg.setSenderRole(record.getSenderRole());
            return msg;
        }).collect(Collectors.toList());
    }
}
