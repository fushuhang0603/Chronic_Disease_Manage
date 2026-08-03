package com.chronicdisease.remind.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新增公告入参
 */
@Data
@Schema(description = "新增公告入参")
public class NoticeDTO {

    @Schema(description = "公告ID，编辑时传入")
    private Long id;

    @Schema(description = "公告标题")
    @NotBlank(message = "公告标题不能为空")
    private String title;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "可见范围: all-全部/patient-患者/doctor-医生")
    private String scope;

    @Schema(description = "状态: 0草稿/1直接发布")
    private Integer status;


    @Schema(description = "发布时间，不传则发布时取当前时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime publishTime;

}
