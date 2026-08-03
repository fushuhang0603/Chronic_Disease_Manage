package com.chronicdisease.remind.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公告分页查询入参
 */
@Data
@Schema(description = "公告分页查询入参")
public class NoticePageDTO {

    @Schema(description = "公告标题（模糊匹配）")
    private String title;

    @Schema(description = "公告状态")
    private Integer status;

    @Schema(description = "可见范围")
    private String scope;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 20;

}
