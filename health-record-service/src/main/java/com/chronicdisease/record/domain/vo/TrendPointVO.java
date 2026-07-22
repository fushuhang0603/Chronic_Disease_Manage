package com.chronicdisease.record.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TrendPointVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 时间标签：日-"07-22" / 周-"第30周" / 月-"07月" */
    private String timeLabel;

    /** 日均值 / 周均值 / 月均值 */
    private BigDecimal avgValue;

    /** 日内最高值 / 周内峰值 / 月内峰值 */
    private BigDecimal maxValue;

    /** 日内最低值 / 周内低值 / 月内低值 */
    private BigDecimal minValue;

    /** 当日/当周/当月 测量次数 */
    private int recordCount;

    /** 当日明细记录（仅 DAY 粒度返回，用于 tooltip 展开） */
    private List<DetailItem> details;

    @Data
    public static class DetailItem implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long id;
        private BigDecimal indexValue;
        private String unit;
        private LocalDateTime recordTime;
    }
}
