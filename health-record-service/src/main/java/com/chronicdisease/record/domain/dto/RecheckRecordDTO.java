package com.chronicdisease.record.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RecheckRecordDTO {

    @NotBlank(message = "医院名称不能为空")
    private String hospitalName;

    @NotBlank(message = "复查项目编码不能为空")
    private String recheckItemCode;

    private String recheckResult;

    private String doctorAdvice;

    @NotNull(message = "实际复查日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualRecheckTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planNextTime;
}
