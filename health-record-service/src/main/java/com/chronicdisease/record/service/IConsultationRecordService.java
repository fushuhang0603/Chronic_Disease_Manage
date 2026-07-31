package com.chronicdisease.record.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.record.domain.entity.ConsultationRecord;

public interface IConsultationRecordService extends IService<ConsultationRecord> {

    /**
     * 异步保存聊天消息
     */
    void saveMessage(Long patientId, String patientName,
                     Long doctorId, String doctorName,
                     Long senderId, String senderRole,
                     String content);
}
