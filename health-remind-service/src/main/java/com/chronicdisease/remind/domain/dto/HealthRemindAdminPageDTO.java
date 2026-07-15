package com.chronicdisease.remind.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端分页查询提醒 DTO（支持按患者姓名筛选）
 */
@Data
@Schema(description = "管理端提醒分页查询")
public class HealthRemindAdminPageDTO extends HealthRemindPageDTO {

    @Schema(description = "患者姓名（模糊匹配）")
    private String patientName;
}
