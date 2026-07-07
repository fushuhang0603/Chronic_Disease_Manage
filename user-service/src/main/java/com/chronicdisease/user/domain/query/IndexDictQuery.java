package com.chronicdisease.user.domain.query;

import lombok.Data;

@Data
public class IndexDictQuery {

    private String indexCode;
    private String indexName;
    private String termType;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 50;

}
