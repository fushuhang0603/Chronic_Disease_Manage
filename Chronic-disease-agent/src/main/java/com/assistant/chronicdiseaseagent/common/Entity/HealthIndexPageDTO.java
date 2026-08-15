package com.assistant.chronicdiseaseagent.common.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 健康指标分页查询入参 — 对齐 health-record-service HealthIndexPageDTO
 */
@Data
public class HealthIndexPageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    private String indexCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
