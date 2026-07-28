package com.chronicdisease.record.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.record.domain.dto.ConsultationMessageDTO;
import com.chronicdisease.record.domain.dto.ConsultationPageDTO;
import com.chronicdisease.record.domain.entity.ConsultationRecord;
import com.chronicdisease.record.domain.vo.ConsultationMessageVO;
import com.chronicdisease.record.domain.vo.DoctorPatientVO;

import java.util.LinkedHashMap;
import java.util.List;

public interface IConsultationService extends IService<ConsultationRecord> {

    /**
     * 发送消息（WebSocket 和 REST 通用）
     * @param senderId   发送人ID
     * @param senderRole 发送人角色 PATIENT / DOCTOR
     * @param dto        消息内容
     * @return 保存后的消息记录
     */
    ConsultationRecord sendMessage(Long senderId, String senderRole, ConsultationMessageDTO dto);

    /**
     * 查询聊天历史（按天分组，时间正序）
     */
    LinkedHashMap<String, List<ConsultationMessageVO>> pageHistory(ConsultationPageDTO dto);

    /**
     * 标记已读：将对方发给当前用户的消息全部标记为已读，并清Redis未读计数
     * @param readerId 当前用户ID
     * @param patientId 患者ID
     * @param doctorId 医生ID
     */
    void markRead(Long readerId, Long patientId, Long doctorId);

    /**
     * 医生端：获取我的患者列表（含最新消息预览、未读数、在线状态）
     * @param doctorId 医生ID
     */
    List<DoctorPatientVO> getDoctorPatients(Long doctorId);
}
