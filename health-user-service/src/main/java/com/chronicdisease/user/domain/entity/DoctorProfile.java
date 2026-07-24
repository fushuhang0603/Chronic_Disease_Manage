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
@TableName("doctor_profile")
@Schema(description = "医生资历表实体类")
public class DoctorProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "资历ID")
    private Long id;

    @TableField("doctor_id")
    @Schema(description = "关联医生用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    @TableField("real_name")
    @Schema(description = "真实姓名")
    private String realName;

    @TableField("title")
    @Schema(description = "职称")
    private String title;

    @TableField("hospital")
    @Schema(description = "所属医院")
    private String hospital;

    @TableField("department")
    @Schema(description = "科室")
    private String department;

    @TableField("specialty")
    @Schema(description = "擅长领域")
    private String specialty;

    @TableField("introduction")
    @Schema(description = "简介")
    private String introduction;

    @TableField("avatar")
    @Schema(description = "头像地址")
    private String avatar;

    @TableField(exist = false)
    @Schema(description = "医生账号（联查user表，不入库）")
    private String doctorName;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
