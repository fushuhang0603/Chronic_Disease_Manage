package com.chronicdisease.record.domain.dto;

import lombok.Data;

@Data
public class RecheckRecordPageDTO {

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    private String recheckItemCode;
}
