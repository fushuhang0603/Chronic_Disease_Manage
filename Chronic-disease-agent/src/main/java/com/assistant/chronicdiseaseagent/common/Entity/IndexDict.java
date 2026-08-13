package com.assistant.chronicdiseaseagent.common.Entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 指标字典 DTO — Feign 调用 health-user-service /dict/page 的反序列化载体
 */
@Data
public class IndexDict implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 术语编码 */
    private String indexCode;

    /** 术语名称 */
    private String indexName;

    /** 术语类型: indicator/disease/medicine */
    private String termType;

    /** 指标正常范围-最低值 */
    private BigDecimal minValue;

    /** 指标正常范围-最高值 */
    private BigDecimal maxValue;
}
