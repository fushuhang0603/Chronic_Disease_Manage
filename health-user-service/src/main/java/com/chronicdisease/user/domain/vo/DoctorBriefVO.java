package com.chronicdisease.user.domain.vo;

import lombok.Data;

/**
 * 医生-患者绑定简要信息（供内部 Feign 调用返回）
 */
@Data
public class DoctorBriefVO {

    /** 医生用户ID */
    private Long doctorId;

    /** 医生姓名 */
    private String doctorName;

    /** 患者姓名 */
    private String patientName;
}
