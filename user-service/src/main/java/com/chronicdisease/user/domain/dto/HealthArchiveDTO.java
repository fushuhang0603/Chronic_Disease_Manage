package com.chronicdisease.user.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class HealthArchiveDTO {

    private Long id;

    @NotBlank(message = "患者姓名不能为空")
    private String patientName;

    private String idCard;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    private String phone;

    private String address;

    private String bloodType;

    @NotBlank(message = "慢病类型不能为空")
    private String chronicType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate diagnosisDate;

    private String medicalHistory;

    private String familyHistory;

    private String allergyHistory;

    private String lifeHabit;

    private String emergencyName;

    private String emergencyPhone;
}
