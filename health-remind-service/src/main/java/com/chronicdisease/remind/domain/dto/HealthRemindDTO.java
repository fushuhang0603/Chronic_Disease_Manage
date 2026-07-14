package com.chronicdisease.remind.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "新增健康提醒入参")
public class HealthRemindDTO {

    @Schema(description = "提醒类型")
    private String remindType;

    @Schema(description = "提醒标题")
    private String title;

    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "提醒时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime remindTime;

    @Schema(description = "重复时间规则每天/每周/每月")
    private String repeatType;

    @Schema(description = "关联业务记录ID，手动创建不传，后续自动生成才用")
    private Long sourceId;

}
