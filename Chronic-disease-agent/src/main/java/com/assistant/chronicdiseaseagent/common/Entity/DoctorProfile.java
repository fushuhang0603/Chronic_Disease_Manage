package com.assistant.chronicdiseaseagent.common.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 医生资历 DTO — Feign 远程调用的反序列化载体
 */
@Data
public class DoctorProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long doctorId;
    private String realName;
    private String title;
    private String hospital;
    private String department;
    private String specialty;
    private String introduction;
    private String avatar;
    private String doctorName;
}
