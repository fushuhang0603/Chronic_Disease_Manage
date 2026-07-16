package com.chronicdisease.article.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("health_article")
public class HealthArticle {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 资讯标题 */
    private String title;

    /** 文章正文 */
    private String content;

    /** 分类：饮食/运动/用药/慢病常识/并发症预防 */
    private String category;

    /** 浏览量 */
    private Integer viewCount;

    /** 0下架 1正常展示 */
    private Integer status;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime publishingTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /** 逻辑删除 */
    private Integer isDeleted;

    /** 是否已收藏（前端展示用，非表字段） */
    @TableField(exist = false)
    private Boolean isFavorited;
}
