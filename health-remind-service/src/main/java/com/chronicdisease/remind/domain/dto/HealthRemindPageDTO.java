package com.chronicdisease.remind.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "健康提醒分页查询入参")
public class HealthRemindPageDTO {

    @Schema(description = "提醒类型")
    private String remindType;
    @Schema(description = "提醒状态")
    private Integer remindStatus;
    @Schema(description = "页码")
    private Integer pageNum = 1;
    @Schema(description = "每页数量")
    private Integer pageSize = 20;

}
