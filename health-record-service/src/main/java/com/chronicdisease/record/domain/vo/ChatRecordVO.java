package com.chronicdisease.record.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天记录 VO，与 WebSocket 推送的 ChatMessage 结构对齐，
 * 前端可复用同一套消息渲染逻辑
 */
@Data
public class ChatRecordVO {

    private Long id;

    /** 发送人用户ID */
    private String fromUserId;

    /** 接收人用户ID */
    private String toUserId;

    /** 消息内容 */
    private String content;

    /** 消息时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime time;

    /** 发送人角色: PATIENT / DOCTOR */
    private String senderRole;

    /** 发送人姓名 */
    private String senderName;
}
