package com.chronicdisease.record.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.record.domain.entity.ConsultationRecord;
import com.chronicdisease.record.mapper.ConsultationRecordMapper;
import com.chronicdisease.record.service.IConsultationRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
}
