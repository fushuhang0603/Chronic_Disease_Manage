package com.chronicdisease.record.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("consultation_record")
@Schema(description = "医患沟通记录表实体类")
public class ConsultationRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "消息ID")
    private Long id;

    @TableField("patient_id")
    @Schema(description = "患者用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    @TableField("patient_name")
    @Schema(description = "患者姓名")
    private String patientName;

    @TableField("doctor_id")
    @Schema(description = "医生用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    @TableField("doctor_name")
    @Schema(description = "医生姓名")
    private String doctorName;

    @TableField("sender_id")
    @Schema(description = "发送人用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long senderId;

    @TableField("sender_role")
    @Schema(description = "发送人角色: PATIENT / DOCTOR")
    private String senderRole;

    @TableField("content")
    @Schema(description = "消息内容")
    private String content;

    @TableField("is_read")
    @Schema(description = "是否已读: 0未读, 1已读")
    private Integer isRead;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField("is_deleted")
    @Schema(description = "逻辑删除: 0未删, 1已删")
    private Integer isDeleted;
}
