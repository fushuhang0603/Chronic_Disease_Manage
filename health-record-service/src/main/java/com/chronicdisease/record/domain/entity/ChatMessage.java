package com.chronicdisease.record.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {

    /** 发送人用户ID（服务端根据session填充，客户端不可信） */
    private String fromUserId;

    /** 接收人用户ID */
    private String toUserId;

    /** 消息内容 */
    private String content;

    /** 消息时间 yyyy-MM-dd HH:mm:ss */
    private String time;

    /** 患者用户ID */
    private String patientId;

    /** 患者姓名 */
    private String patientName;

    /** 医生用户ID */
    private String doctorId;

    /** 医生姓名 */
    private String doctorName;

    /** 发送人角色: PATIENT / DOCTOR */
    private String senderRole;

    /** 发送人姓名 */
    private String senderName;
}
