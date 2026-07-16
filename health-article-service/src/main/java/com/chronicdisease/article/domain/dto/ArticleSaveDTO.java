package com.chronicdisease.article.domain.dto;

import lombok.Data;

@Data
public class ArticleSaveDTO {

    /** 编辑时传ID，新增时不传 */
    private Long id;

    /** 资讯标题 */
    private String title;

    /** 文章正文 */
    private String content;

    /** 分类 */
    private String category;

    /** 发布时间 */
    private String publishingTime;

    /** 0下架 1上架 */
    private Integer status;
}
