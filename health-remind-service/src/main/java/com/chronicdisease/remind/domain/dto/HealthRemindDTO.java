package com.chronicdisease.remind.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthRemindDTO {

    @NotBlank(message = "提醒类型不能为空")
    private String remindType;

    @NotBlank(message = "提醒标题不能为空")
    private String title;

    private String content;

    @NotNull(message = "提醒时间不能为空")
    private LocalDateTime remindTime;

    private String repeatType;

    private Long sourceId;

}
