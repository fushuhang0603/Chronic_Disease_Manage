package com.chronicdisease.remind.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "系统公告表实体类")
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "公告ID")
    private Long id;

    @TableField("title")
    @Schema(description = "公告标题")
    private String title;

    @TableField("content")
    @Schema(description = "公告内容")
    private String content;

    @TableField("scope")
    @Schema(description = "可见范围: all-全部/patient-患者/doctor-医生")
    private String scope;

    @TableField("status")
    @Schema(description = "状态: 0草稿/1已发布/2已下线")
    private Integer status;

    @TableField("publisher_id")
    @Schema(description = "发布人ID")
    private Long publisherId;


    @TableField("publish_time")
    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime publishTime;

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
