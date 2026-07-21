package com.chronicdisease.record.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("recheck_record")
@Schema(description = "复查记录表实体类")
public class RecheckRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "复查记录ID")
    private Long id;

    @TableField("user_id")
    @Schema(description = "患者用户ID")
    private Long userId;

    @TableField("hospital_name")
    @Schema(description = "复查医院名称")
    private String hospitalName;

    @TableField("recheck_item_code")
    @Schema(description = "复查项目编码，关联术语字典 term_dict.term_code（term_type=indicator 或 disease）")
    private String recheckItemCode;

    @TableField("recheck_result")
    @Schema(description = "检查结果描述")
    private String recheckResult;

    @TableField("doctor_advice")
    @Schema(description = "医生诊断建议")
    private String doctorAdvice;

    @TableField("actual_recheck_time")
    @Schema(description = "实际复查日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate actualRecheckTime;

    @TableField("plan_next_time")
    @Schema(description = "计划下次复查日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate planNextTime;

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
