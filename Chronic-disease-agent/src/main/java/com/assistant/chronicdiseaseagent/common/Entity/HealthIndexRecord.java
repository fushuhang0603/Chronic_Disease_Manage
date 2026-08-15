package com.assistant.chronicdiseaseagent.common.Entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 健康指标记录 DTO — Feign 调用 health-record-service /index/page 的反序列化载体
 */
@Data
public class HealthIndexRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long id;

    /** 患者用户ID（后端使用 ToStringSerializer，JSON 中为字符串） */
    private String userId;

    /** 指标编码 */
    private String indexCode;

    /** 指标单位，如 mmHg, mmol/L */
    private String unit;

    /** 指标数值 */
    private BigDecimal indexValue;

    /** 测量时间（yyyy-MM-dd HH:mm:ss） */
    private String recordTime;

    /** 备注：测量场景、身体状态 */
    private String remark;

    /** 是否异常 0-正常 1-偏高 2-偏低 */
    private Integer isAbnormal;
}
