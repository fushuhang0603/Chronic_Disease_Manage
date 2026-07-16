package com.chronicdisease.article.domain.dto;

import lombok.Data;

@Data
public class ReadHistoryDTO {

    /** 资讯ID */
    private Long articleId;

    /** 阅读时长（秒） */
    private Integer readDuration;
}
