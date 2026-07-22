package com.chronicdisease.record.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("medicine_record")
@Schema(description = "患者用药记录表实体类")
public class MedicineRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "用药记录ID")
    private Long id;

    @TableField("user_id")
    @Schema(description = "患者用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @TableField("drug_code")
    @Schema(description = "药品编码，关联术语字典 term_dict.term_code（term_type=medicine）")
    private String drugCode;

    @TableField("dosage")
    @Schema(description = "服用剂量，如：0.5g/次、1片/次")
    private String dosage;

    @TableField("frequency")
    @Schema(description = "服用频次，如：每日2次、早晚各1片")
    private String frequency;

    @TableField("start_date")
    @Schema(description = "开始服药日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startDate;

    @TableField("stop_date")
    @Schema(description = "停药日期，为空表示持续服用中")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate stopDate;

    @TableField("remark")
    @Schema(description = "医嘱备注、不良反应记录")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @Schema(description = "是否删除 1-删除 0-未删除")
    private Integer isDeleted;

    @TableField(exist = false)
    @Schema(description = "患者姓名（非数据库字段）")
    private String patientName;
}
