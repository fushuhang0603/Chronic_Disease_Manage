package com.chronicdisease.remind.domain.dto;

import lombok.Data;

@Data
public class HealthRemindPageDTO {

    private String remindType;
    private Integer remindStatus;
    private Integer pageNum = 1;
    private Integer pageSize = 20;

}
