package com.chronicdisease.record.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DashboardAbnormalItem {

    private Long patientId;

    private String patientName;

    private String indexCode;

    private BigDecimal indexValue;

    private Integer isAbnormal;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime recordTime;
}
