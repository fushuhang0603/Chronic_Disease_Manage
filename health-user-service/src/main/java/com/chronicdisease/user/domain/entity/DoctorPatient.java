package com.chronicdisease.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("doctor_patient")
@Schema(description = "医生患者绑定表实体类")
public class DoctorPatient implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "绑定ID")
    private Long id;

    @TableField("doctor_id")
    @Schema(description = "医生用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    @TableField("doctor_name")
    @Schema(description = "医生姓名")
    private String doctorName;

    @TableField("patient_id")
    @Schema(description = "患者用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    @TableField("patient_name")
    @Schema(description = "患者姓名")
    private String patientName;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "绑定时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField("is_deleted")
    @Schema(description = "是否删除 1-删除 0-未删除")
    private Integer isDeleted;
}
