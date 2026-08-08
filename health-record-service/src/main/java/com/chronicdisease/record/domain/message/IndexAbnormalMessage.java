package com.chronicdisease.record.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 指标异常通知消息体（投递到 RabbitMQ）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexAbnormalMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 患者用户ID */
    private Long patientId;

    /** 患者姓名 */
    private String patientName;

    /** 指标编码 */
    private String indexCode;

    /** 指标名称（如：收缩压、空腹血糖） */
    private String indexName;

    /** 指标数值 */
    private BigDecimal indexValue;

    /** 指标单位 */
    private String unit;

    /** 异常标签：偏高 / 偏低 */
    private String abnormalLabel;

    /** 记录时间 */
    private LocalDateTime recordTime;

    /** 指标记录ID（供医生点击跳转查看详情） */
    private Long recordId;
}
