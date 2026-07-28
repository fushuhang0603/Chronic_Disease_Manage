package com.chronicdisease.record.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "聊天消息VO")
public class ConsultationMessageVO {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "发送人ID")
    private Long senderId;

    @Schema(description = "发送人角色 PATIENT/DOCTOR")
    private String senderRole;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息时间 HH:mm")
    private String createTime;
}
