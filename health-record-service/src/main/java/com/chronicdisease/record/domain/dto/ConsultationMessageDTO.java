package com.chronicdisease.record.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "发送消息请求体")
public class ConsultationMessageDTO {

    @NotNull(message = "患者ID不能为空")
    @Schema(description = "患者用户ID")
    private Long patientId;

    @NotNull(message = "医生ID不能为空")
    @Schema(description = "医生用户ID")
    private Long doctorId;

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "消息内容")
    private String content;
}
