package com.chronicdisease.user.domain.vo;

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
    private Long userId;
    private String patientName;
}
