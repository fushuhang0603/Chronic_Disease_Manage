package com.chronicdisease.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "指标术语简要信息")
public class IndexDictBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "术语编码")
    private String indexCode;

    @Schema(description = "术语名称")
    private String indexName;

    @Schema(description = "术语类型: indicator/disease/medicine")
    private String termType;

    @Schema(description = "指标正常范围-最低值")
    private BigDecimal minValue;

    @Schema(description = "指标正常范围-最高值")
    private BigDecimal maxValue;

    @Schema(description = "状态 1-启用,0-禁用")
    private Integer status;
}
