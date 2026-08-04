package com.chronicdisease.record.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 聊天记录分页查询 DTO
 */
@Data
public class ConsultationPageDTO {

    @Schema(description = "页码，从 1 开始，默认 1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，默认 20")
    private Integer pageSize = 20;

    @Schema(description = "对端用户ID（医生传患者ID / 患者传医生ID）")
    private Long otherUserId;
}
