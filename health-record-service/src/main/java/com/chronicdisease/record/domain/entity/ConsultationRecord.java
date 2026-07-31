package com.chronicdisease.record.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("consultation_record")
public class ConsultationRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("patient_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    @TableField("patient_name")
    private String patientName;

    @TableField("doctor_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    @TableField("doctor_name")
    private String doctorName;

    @TableField("sender_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long senderId;

    @TableField("sender_role")
    private String senderRole;

    @TableField("content")
    private String content;

    @TableField("is_read")
    private Integer isRead;

    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField("create_user")
    private Long createUser;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    @TableField("update_user")
    private Long updateUser;
}
