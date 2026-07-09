package com.chronicdisease.record.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HealthIndexDTO {

    @NotBlank(message = "指标编码不能为空")
    private String indexCode;

    private String unit;

    @NotNull(message = "指标数值不能为空")
    private BigDecimal indexValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;

    private String remark;
}
