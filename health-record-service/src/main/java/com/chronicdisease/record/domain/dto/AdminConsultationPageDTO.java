package com.chronicdisease.record.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用于管理端咨询记录分页查询")
public class AdminConsultationPageDTO extends ConsultationPageDTO{

    @Schema(description = "医生名称")
    private String doctorName;

    @Schema(description = "患者名称")
    private String patientName;

    @Schema(description = "聊天内容")
    private String content;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

}
