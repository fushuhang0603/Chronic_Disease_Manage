package com.chronicdisease.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("health_archive")
@Schema(description = "健康档案表实体类")
public class HealthArchive implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "档案ID")
    private Long id;

    @TableField("patient_name")
    @Schema(description = "患者姓名")
    private String patientName;

    @TableField("id_card")
    @Schema(description = "身份证号")
    private String idCard;

    @TableField("birth_date")
    @Schema(description = "出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @TableField("gender")
    @Schema(description = "性别 0未知 1男 2女")
    private Integer gender;

    @TableField("phone")
    @Schema(description = "联系电话")
    private String phone;

    @TableField("address")
    @Schema(description = "家庭住址")
    private String address;

    @TableField("blood_type")
    @Schema(description = "血型")
    private String bloodType;

    @TableField("chronic_type")
    @Schema(description = "慢病类型")
    private String chronicType;

    @TableField("diagnosis_date")
    @Schema(description = "首次确诊日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate diagnosisDate;

    @TableField("medical_history")
    @Schema(description = "既往病史/手术史")
    private String medicalHistory;

    @TableField("family_history")
    @Schema(description = "家族遗传病史")
    private String familyHistory;

    @TableField("allergy_history")
    @Schema(description = "药物/食物过敏史")
    private String allergyHistory;

    @TableField("life_habit")
    @Schema(description = "生活习惯")
    private String lifeHabit;

    @TableField("emergency_name")
    @Schema(description = "紧急联系人")
    private String emergencyName;

    @TableField("emergency_phone")
    @Schema(description = "紧急联系人电话")
    private String emergencyPhone;

    @TableField("bind_doctor_id")
    @Schema(description = "绑定负责医生ID")
    private Long bindDoctorId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @Schema(description = "是否删除 1-删除 0-未删除")
    private Integer isDeleted;
}
