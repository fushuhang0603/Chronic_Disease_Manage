package com.chronicdisease.record.domain.dto;

import lombok.Data;

/**
 * 聊天记录分页查询 DTO
 */
@Data
public class ConsultationPageDTO {

    /** 页码，从 1 开始，默认 1 */
    private Integer pageNum = 1;

    /** 每页条数，默认 20 */
    private Integer pageSize = 20;

    /** 对端用户ID（医生传患者ID / 患者传医生ID） */
    private Long otherUserId;
}
