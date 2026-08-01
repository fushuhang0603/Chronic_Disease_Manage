package com.chronicdisease.user.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 患者简要信息（供远程调用返回）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientBriefVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String patientName;
}
