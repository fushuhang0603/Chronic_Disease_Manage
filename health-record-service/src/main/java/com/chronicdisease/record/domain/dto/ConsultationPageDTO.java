package com.chronicdisease.record.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "分页查询聊天记录请求体")
public class ConsultationPageDTO {

    @NotNull(message = "患者ID不能为空")
    @Schema(description = "患者用户ID")
    private Long patientId;

    @NotNull(message = "医生ID不能为空")
    @Schema(description = "医生用户ID")
    private Long doctorId;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "20")
    private Integer pageSize = 20;
}
