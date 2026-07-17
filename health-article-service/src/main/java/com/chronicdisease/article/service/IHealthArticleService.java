package com.chronicdisease.article.service;

import com.chronicdisease.article.domain.dto.*;
import com.chronicdisease.article.domain.entity.HealthArticle;
import com.chronicdisease.common.result.PageResult;


public interface IHealthArticleService {

    /** 患者端分页（仅上架资讯） */
    PageResult<HealthArticle> pageArticle(ArticlePageDTO dto);

    /** 患者端分页查询我的收藏 */
    PageResult<HealthArticle> pageFavorites(ArticlePageDTO dto);

    /** 管理端分页（可查所有状态） */
    PageResult<HealthArticle> pageArticleAdmin(ArticlePageDTO dto);

    /** 查询文章详情（浏览量+1） */
    HealthArticle getDetail(Long id);

    /** 新增资讯 */
    void addArticle(ArticleSaveDTO dto);

    /** 编辑资讯 */
    void updateArticle(ArticleSaveDTO dto);

    /** 删除资讯 */
    void deleteArticle(Long id);

    /** 上下架 */
    void updateStatus(Long id, Integer status);

    /** 收藏/取消收藏 */
    void updateFavoriteStatus(Long articleId, Integer status);

    /** 记录阅读历史 */
    void recordReadHistory(ReadHistoryDTO dto);
}
