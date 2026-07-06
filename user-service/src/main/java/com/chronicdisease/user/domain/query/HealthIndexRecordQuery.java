package com.chronicdisease.user.domain.query;

import lombok.Data;
import java.time.LocalDate;

@Data
public class HealthIndexRecordQuery {

    private String indexCode;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer pageNum = 1;

    private Integer pageSize = 20;
}
