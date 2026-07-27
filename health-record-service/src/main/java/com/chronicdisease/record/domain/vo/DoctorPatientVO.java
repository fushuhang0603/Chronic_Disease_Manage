package com.chronicdisease.record.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "医生端患者列表展示VO")
public class DoctorPatientVO {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "患者用户ID")
    private Long patientId;

    @Schema(description = "患者姓名")
    private String patientName;

    @Schema(description = "最新消息内容预览")
    private String latestContent;

    @Schema(description = "最新消息时间")
    private String latestTime;

    @Schema(description = "未读消息数")
    private Integer unreadCount;

    @Schema(description = "是否在线: true在线, false离线")
    private Boolean online;
}
