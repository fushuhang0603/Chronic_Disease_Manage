package com.chronicdisease.article.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chronicdisease.article.domain.dto.*;
import com.chronicdisease.article.domain.entity.ArticleFavorite;
import com.chronicdisease.article.domain.entity.ArticleReadHistory;
import com.chronicdisease.article.domain.entity.HealthArticle;
import com.chronicdisease.article.mapper.ArticleFavoriteMapper;
import com.chronicdisease.article.mapper.ArticleReadHistoryMapper;
import com.chronicdisease.article.mapper.HealthArticleMapper;
import com.chronicdisease.article.service.IHealthArticleService;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.util.UserInfoContext;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthArticleServiceImpl implements IHealthArticleService {

    @Resource
    private HealthArticleMapper healthArticleMapper;
    @Resource
    private ArticleFavoriteMapper articleFavoriteMapper;
    @Resource
    private ArticleReadHistoryMapper articleReadHistoryMapper;

    @Override
    public PageResult<HealthArticle> pageArticle(ArticlePageDTO dto) {
        Long userId = UserInfoContext.getUserId();

        LambdaQueryWrapper<HealthArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthArticle::getIsDeleted, BusinessConstant.isNotDelete);

        // 分类筛选
        if (StringUtils.isNotBlank(dto.getCategory())) {
            wrapper.eq(HealthArticle::getCategory, dto.getCategory());
        }
        // 关键词搜索
        if (StringUtils.isNotBlank(dto.getKeyword())) {
            wrapper.like(HealthArticle::getTitle, dto.getKeyword());
        }
        // 状态筛选（管理端传，患者端不传则只查上架的）
        if (dto.getStatus() != null) {
            wrapper.eq(HealthArticle::getStatus, dto.getStatus());
        } else {
            wrapper.eq(HealthArticle::getStatus, 1);
        }

        // 仅查已收藏（患者端用）
        if (dto.getOnlyFavorited() != null && dto.getOnlyFavorited() && userId != null) {
            LambdaQueryWrapper<ArticleFavorite> favQuery = new LambdaQueryWrapper<>();
            favQuery.eq(ArticleFavorite::getUserId, userId)
                    .eq(ArticleFavorite::getCollectStatus, BusinessConstant.Collect_STATUS1);
            List<Long> favArticleIds = articleFavoriteMapper.selectList(favQuery)
                    .stream().map(ArticleFavorite::getArticleId).collect(Collectors.toList());
            if (favArticleIds.isEmpty()) {
                return new PageResult<>(List.of(), 0L);
            }
            wrapper.in(HealthArticle::getId, favArticleIds);
        }

        wrapper.orderByDesc(HealthArticle::getPublishingTime);

        Page<HealthArticle> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<HealthArticle> result = healthArticleMapper.selectPage(page, wrapper);

        // 标记当前用户是否已收藏
        if (userId != null) {
            List<Long> favoritedIds = getFavoritedArticleIds(userId);
            result.getRecords().forEach(a -> a.setIsFavorited(favoritedIds.contains(a.getId())));
        }

        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public HealthArticle getDetail(Long id) {
        HealthArticle article = healthArticleMapper.selectById(id);
        if (article == null || article.getIsDeleted().equals(1)) {
            throw new BusinessException("资讯不存在或已删除");
        }
        if (article.getStatus().equals(0)) {
            throw new BusinessException("该资讯已下架");
        }
        // 浏览量 +1
        LambdaUpdateWrapper<HealthArticle> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HealthArticle::getId, id).setSql("view_count = view_count + 1");
        healthArticleMapper.update(null, updateWrapper);

        // 标记是否已收藏
        Long userId = UserInfoContext.getUserId();
        if (userId != null) {
            article.setIsFavorited(isFavorited(userId, id));
        }
        return article;
    }

    @Override
    @Transactional
    public void addArticle(ArticleSaveDTO dto) {
        HealthArticle article = new HealthArticle();
        BeanUtil.copyProperties(dto, article);
        article.setViewCount(0);
        if (article.getStatus() == null) {
            article.setStatus(1);
        }
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        healthArticleMapper.insert(article);
    }

    @Override
    @Transactional
    public void updateArticle(ArticleSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("编辑操作必须传ID");
        }
        HealthArticle exist = healthArticleMapper.selectById(dto.getId());
        if (exist == null) {
            throw new BusinessException("资讯不存在");
        }
        HealthArticle article = new HealthArticle();
        BeanUtil.copyProperties(dto, article);
        article.setUpdateTime(LocalDateTime.now());
        healthArticleMapper.updateById(article);
    }

    @Override
    public void deleteArticle(Long id) {
        HealthArticle article = healthArticleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("资讯不存在");
        }
        article.setIsDeleted(1);
        article.setUpdateTime(LocalDateTime.now());
        healthArticleMapper.updateById(article);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        HealthArticle article = healthArticleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("资讯不存在");
        }
        article.setStatus(status);
        article.setUpdateTime(LocalDateTime.now());
        healthArticleMapper.updateById(article);
    }

    @Override
    @Transactional
    public void toggleFavorite(FavoriteDTO dto) {
        Long userId = UserInfoContext.getUserId();

        // 查找现有收藏记录（含已取消的）
        LambdaQueryWrapper<ArticleFavorite> query = new LambdaQueryWrapper<>();
        query.eq(ArticleFavorite::getUserId, userId)
                .eq(ArticleFavorite::getArticleId, dto.getArticleId());
        ArticleFavorite favorite = articleFavoriteMapper.selectOne(query);

        if (favorite == null) {
            // 首次收藏
            favorite = new ArticleFavorite();
            favorite.setUserId(userId);
            favorite.setArticleId(dto.getArticleId());
            favorite.setCollectStatus(BusinessConstant.Collect_STATUS1);
            favorite.setCreateTime(LocalDateTime.now());
            favorite.setUpdateTime(LocalDateTime.now());
            articleFavoriteMapper.insert(favorite);
        } else if (favorite.getCollectStatus().equals(BusinessConstant.Collect_STATUS1)) {
            // 取消收藏
            favorite.setCollectStatus(BusinessConstant.Collect_STATUS2);
            favorite.setUpdateTime(LocalDateTime.now());
            articleFavoriteMapper.updateById(favorite);
        } else {
            // 重新收藏
            favorite.setCollectStatus(BusinessConstant.Collect_STATUS1);
            favorite.setUpdateTime(LocalDateTime.now());
            articleFavoriteMapper.updateById(favorite);
        }
    }

    @Override
    public void recordReadHistory(ReadHistoryDTO dto) {
        Long userId = UserInfoContext.getUserId();

        // 同一天同文章只更新时间不新增行
        LambdaQueryWrapper<ArticleReadHistory> query = new LambdaQueryWrapper<>();
        query.eq(ArticleReadHistory::getUserId, userId)
                .eq(ArticleReadHistory::getArticleId, dto.getArticleId())
                .eq(ArticleReadHistory::getIsDeleted, BusinessConstant.isNotDelete)
                .orderByDesc(ArticleReadHistory::getReadTime)
                .last("LIMIT 1");
        ArticleReadHistory history = articleReadHistoryMapper.selectOne(query);

        if (history != null && history.getReadTime().toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
            // 今天已读过，更新时长
            history.setReadDuration(
                    (history.getReadDuration() == null ? 0 : history.getReadDuration())
                            + (dto.getReadDuration() == null ? 0 : dto.getReadDuration())
            );
            history.setReadTime(LocalDateTime.now());
            articleReadHistoryMapper.updateById(history);
        } else {
            // 新记录
            history = new ArticleReadHistory();
            history.setUserId(userId);
            history.setArticleId(dto.getArticleId());
            history.setReadDuration(dto.getReadDuration());
            history.setReadTime(LocalDateTime.now());
            history.setCreateTime(LocalDateTime.now());
            history.setIsDeleted(BusinessConstant.isNotDelete);
            articleReadHistoryMapper.insert(history);
        }
    }

    /** 获取当前用户已收藏的文章ID列表 */
    private List<Long> getFavoritedArticleIds(Long userId) {
        LambdaQueryWrapper<ArticleFavorite> query = new LambdaQueryWrapper<>();
        query.eq(ArticleFavorite::getUserId, userId)
                .eq(ArticleFavorite::getCollectStatus, BusinessConstant.Collect_STATUS1);
        return articleFavoriteMapper.selectList(query)
                .stream().map(ArticleFavorite::getArticleId).collect(Collectors.toList());
    }

    /** 判断当前用户是否已收藏某文章 */
    private boolean isFavorited(Long userId, Long articleId) {
        LambdaQueryWrapper<ArticleFavorite> query = new LambdaQueryWrapper<>();
        query.eq(ArticleFavorite::getUserId, userId)
                .eq(ArticleFavorite::getArticleId, articleId)
                .eq(ArticleFavorite::getCollectStatus, BusinessConstant.Collect_STATUS1);
        return articleFavoriteMapper.selectCount(query) > 0;
    }
}
