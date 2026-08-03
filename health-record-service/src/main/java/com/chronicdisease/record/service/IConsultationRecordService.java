package com.chronicdisease.record.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.record.domain.dto.ConsultationPageDTO;
import com.chronicdisease.record.domain.entity.ConsultationRecord;
import com.chronicdisease.record.domain.vo.ChatRecordVO;

public interface IConsultationRecordService extends IService<ConsultationRecord> {

    /**
     * 异步保存聊天消息
     */
    void saveMessage(Long patientId, String patientName,
                     Long doctorId, String doctorName,
                     Long senderId, String senderRole,
                     String content);

    /**
     * 分页查询当前用户与对端用户的聊天记录（时间倒序，最新在前）
     */
    PageResult<ChatRecordVO> pageRecords(ConsultationPageDTO dto);

    /**
     * 将当前用户与对端用户会话中收到的未读消息标记为已读
     */
    void markRead(Long otherUserId);
}
