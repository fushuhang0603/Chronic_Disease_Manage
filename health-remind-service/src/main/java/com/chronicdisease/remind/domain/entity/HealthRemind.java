package com.chronicdisease.remind.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("health_remind")
@Schema(description = "健康提醒表实体类")
public class HealthRemind implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "提醒ID")
    private Long id;

    @TableField("user_id")
    @Schema(description = "患者用户ID")
    private Long userId;

    @TableField("remind_type")
    @Schema(description = "提醒类型: medicine-用药/recheck-复查/custom-自定义")
    private String remindType;

    @TableField("title")
    @Schema(description = "提醒标题")
    private String title;

    @TableField("content")
    @Schema(description = "提醒详情")
    private String content;

    @TableField("remind_time")
    @Schema(description = "提醒触发时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime remindTime;

    @TableField("repeat_type")
    @Schema(description = "重复规则: none-不重复/daily-每天/weekly-每周/monthly-每月")
    private String repeatType;

    @TableField("remind_status")
    @Schema(description = "状态: 0待提醒 1已推送 2已读 3已完成")
    private Integer remindStatus;

    @TableField("source_id")
    @Schema(description = "关联业务记录ID")
    private Long sourceId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @Schema(description = "是否删除: 0未删除 1已删除")
    private Integer isDeleted;

}
