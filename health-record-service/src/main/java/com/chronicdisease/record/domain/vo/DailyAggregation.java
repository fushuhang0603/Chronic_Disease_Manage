package com.chronicdisease.record.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * SQL 日聚合中间结果，不入库、不返回前端，仅在 Service 层使用
 */
@Data
public class DailyAggregation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String indexCode;
    private java.sql.Date recordDate;
    private BigDecimal avgValue;
    private BigDecimal maxValue;
    private BigDecimal minValue;
    private int recordCount;
}
