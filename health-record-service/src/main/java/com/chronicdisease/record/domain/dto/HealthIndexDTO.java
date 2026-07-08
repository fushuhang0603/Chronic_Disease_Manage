package com.chronicdisease.record.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class HealthIndexDTO {

    @NotBlank(message = "指标编码不能为空")
    private String indexCode;

    private String unit;

    @NotNull(message = "指标数值不能为空")
    private BigDecimal indexValue;

    private String remark;
}
