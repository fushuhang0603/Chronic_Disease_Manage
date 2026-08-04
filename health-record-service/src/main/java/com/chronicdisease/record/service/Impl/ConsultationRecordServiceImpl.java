package com.chronicdisease.record.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.record.domain.dto.AdminConsultationPageDTO;
import com.chronicdisease.record.domain.dto.ConsultationPageDTO;
import com.chronicdisease.record.domain.dto.DayConsultationDTO;
import com.chronicdisease.record.domain.entity.ChatMessage;
import com.chronicdisease.record.domain.entity.ConsultationRecord;
import com.chronicdisease.record.domain.vo.ChatRecordVO;
import com.chronicdisease.record.mapper.ConsultationRecordMapper;
import com.chronicdisease.record.service.IConsultationRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    /**
     * 管理端聊天记录分页查询
     * @param dto
     * @return
     */
    @Override
    public PageResult<ConsultationRecord> pageAdminPage(AdminConsultationPageDTO dto) {
        Integer pageSize = dto.getPageSize() == null ? 30 : dto.getPageSize();
        Integer pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        LambdaQueryWrapper<ConsultationRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(dto.getDoctorName())){
            wrapper.like(ConsultationRecord::getDoctorName, dto.getDoctorName());
        }
        if (StringUtils.isNotBlank(dto.getPatientName())){
            wrapper.like(ConsultationRecord::getPatientName, dto.getPatientName());
        }
        if (dto.getStartTime() != null){
            wrapper.ge(ConsultationRecord::getCreateTime, dto.getStartTime());
        }
        if (dto.getEndTime() != null){
            wrapper.le(ConsultationRecord::getCreateTime, dto.getEndTime());
        }
        if (StringUtils.isNotBlank(dto.getContent())){
            wrapper.like(ConsultationRecord::getContent, dto.getContent());
        }
        wrapper.eq(ConsultationRecord::getIsDeleted, BusinessConstant.isNotDelete);
        Page<ConsultationRecord> page = new Page<>(pageNum,pageSize);
        Page<ConsultationRecord> dbResult = consultationRecordMapper.selectPage(page, wrapper);
        return new PageResult<>(dbResult.getRecords(), dbResult.getTotal());
    }

    @Override
    public List<ConsultationRecord> getDayRecords(DayConsultationDTO dto) {
        if (dto.getDoctorId() == null){
            throw new BusinessException("医生ID不能为空");
        }
        if (dto.getPatientId() == null){
            throw new BusinessException("患者ID不能为空");
        }
        if (dto.getConsultationTime() == null){
            throw new BusinessException("具体日期不能为空");
        }
        LocalDate date = dto.getConsultationTime().toLocalDate();
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        LambdaQueryWrapper<ConsultationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationRecord::getPatientId, dto.getPatientId())
                .eq(ConsultationRecord::getDoctorId, dto.getDoctorId())
                .between(ConsultationRecord::getCreateTime, start, end)
                .eq(ConsultationRecord::getIsDeleted, BusinessConstant.isNotDelete)
                .orderByAsc(ConsultationRecord::getCreateTime);
        return baseMapper.selectList(wrapper);
    }
}
