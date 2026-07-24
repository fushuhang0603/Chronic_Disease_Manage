package com.chronicdisease.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DoctorProfileDTO {

    @Schema(description = "资历id")
    private Long id;

    @NotNull(message = "医生账号不能为空")
    @Schema(description = "用于关联user_id")
    private Long doctorId;

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "医生真实姓名")
    private String realName;

    @NotBlank(message = "职称不能为空")
    @Schema(description = "医生职称")
    private String title;

    @NotBlank(message = "所属医院不能为空")
    @Schema(description = "医生所属医院")
    private String hospital;

    @NotBlank(message = "科室不能为空")
    @Schema(description = "医生科室")
    private String department;

    @NotBlank(message = "擅长领域不能为空")
    @Schema(description = "医生擅长领域")
    private String specialty;

    @Schema(description = "医生简介")
    private String introduction;

    @Schema(description = "医生头像")
    private String avatar;
}
