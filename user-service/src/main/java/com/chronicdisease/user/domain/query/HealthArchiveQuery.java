package com.chronicdisease.user.domain.query;

import lombok.Data;

@Data
public class HealthArchiveQuery {

    private String patientName;
    private String idCard;
    private Integer pageNum = 1;
    private Integer pageSize = 20;

}
