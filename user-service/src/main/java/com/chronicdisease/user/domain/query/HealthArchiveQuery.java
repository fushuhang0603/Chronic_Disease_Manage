package com.chronicdisease.user.domain.query;

import lombok.Data;

@Data
public class HealthArchiveQuery {

    private Long userId;
    private String chronicType;
    private Integer gender;
    private Integer pageNum = 1;
    private Integer pageSize = 10;

}
