package com.chronicdisease.record.domain.vo;

import lombok.Data;

/**
 * 医生-患者绑定简要信息（Feign 跨服务传输）
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
