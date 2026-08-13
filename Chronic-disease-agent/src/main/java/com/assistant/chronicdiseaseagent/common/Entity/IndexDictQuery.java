package com.assistant.chronicdiseaseagent.common.Entity;

import lombok.Data;

/**
 * 指标字典分页查询请求体 — 对齐 health-user-service IndexDictQuery
 */
@Data
public class IndexDictQuery {

    private String indexCode;
    private String indexName;
    private String termType;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 100;
}
