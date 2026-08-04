package com.chronicdisease.record.domain.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Schema(description = "具体某日聊天记录")
public class DayConsultationDTO  {

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "患者ID")
    private Long patientId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime consultationTime;

}
