package com.chronicdisease.article.domain.dto;

import lombok.Data;

@Data
public class ArticlePageDTO {

    private Long pageNum = 1L;
    private Long pageSize = 10L;

    /** 分类筛选：饮食/运动/用药/慢病常识/并发症预防 */
    private String category;

    /** 状态：0下架 1上架，管理端传 */
    private Integer status;

    /** 标题关键词搜索 */
    private String keyword;

    /** 是否仅查看已收藏（患者端用） */
    private Boolean onlyFavorited;
}
