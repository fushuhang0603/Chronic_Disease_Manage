package com.chronicdisease.record.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicineRecordDTO {

    @NotBlank(message = "药品编码不能为空")
    private String drugCode;

    @NotBlank(message = "服用剂量不能为空")
    private String dosage;

    @NotBlank(message = "服用频次不能为空")
    private String frequency;

    @NotNull(message = "开始服药日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate stopDate;

    private String remark;
}
