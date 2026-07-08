package com.chronicdisease.record.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("health_index_record")
@Schema(description = "健康指标监测记录表实体类")
public class HealthIndexRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Long id;

    @TableField("user_id")
    @Schema(description = "患者用户ID")
    private Long userId;

    @TableField("index_code")
    @Schema(description = "指标编码，关联术语字典 term_dict.term_code")
    private String indexCode;

    @TableField("unit")
    @Schema(description = "指标单位，如 mmHg, mmol/L")
    private String unit;

    @TableField("index_value")
    @Schema(description = "指标数值")
    private BigDecimal indexValue;

    @TableField("record_time")
    @Schema(description = "测量时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime recordTime;

    @TableField("remark")
    @Schema(description = "备注：测量场景、身体状态")
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
}
