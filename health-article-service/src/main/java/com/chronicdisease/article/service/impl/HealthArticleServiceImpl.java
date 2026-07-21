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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.ZSetOperations;

@Service
public class HealthArticleServiceImpl implements IHealthArticleService {

    @Autowired
    private HealthArticleMapper healthArticleMapper;
    @Autowired
    private ArticleFavoriteMapper articleFavoriteMapper;
    @Autowired
    private ArticleReadHistoryMapper articleReadHistoryMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_PREFIX = "article:list:";
    private static final long CACHE_TTL = 5L;
    private static final String FAVORITES_RANK_PREFIX = "article:favorites:rank:";

    private String favoritesRankKey() {
        return FAVORITES_RANK_PREFIX + LocalDate.now();
    }

    @Override
    public PageResult<HealthArticle> pageArticle(ArticlePageDTO dto) {
        Long userId = UserInfoContext.getUserId();

        // 从缓存中获取文章列表
        String key = StringUtils.isBlank(dto.getCategory()) ? "all" : dto.getCategory();
        String cacheKey = CACHE_PREFIX + key;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        List<HealthArticle> allArticles;
        if (cached != null) {
            allArticles = (List<HealthArticle>) cached;
        } else {
            LambdaQueryWrapper<HealthArticle> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HealthArticle::getIsDeleted, BusinessConstant.isNotDelete);
            wrapper.eq(HealthArticle::getStatus, BusinessConstant.Article_Status_On);
            if (StringUtils.isNotBlank(dto.getCategory())) {
                wrapper.eq(HealthArticle::getCategory, dto.getCategory());
            }
            wrapper.orderByDesc(HealthArticle::getPublishingTime);
            allArticles = healthArticleMapper.selectList(wrapper);
            // 存入redis
            redisTemplate.opsForValue().set(cacheKey, allArticles, CACHE_TTL, TimeUnit.MINUTES);
        }

        // 内存分页
        int from = (int) ((dto.getPageNum() - 1) * dto.getPageSize());
        int to = Math.min(from + dto.getPageSize().intValue(), allArticles.size());
        List<HealthArticle> pageRecords = from < allArticles.size()
                ? allArticles.subList(from, to)
                : List.of();

        if (userId != null) {
            List<Long> favoritedIds = getFavoritedArticleIds(userId);
            pageRecords.forEach(a -> a.setIsFavorited(favoritedIds.contains(a.getId())));
        }

        return new PageResult<>(pageRecords, (long) allArticles.size());
    }

    @Override
    public PageResult<HealthArticle> pageFavorites(ArticlePageDTO dto) {
        Long userId = UserInfoContext.getUserId();

        // 查用户收藏的文章ID
        LambdaQueryWrapper<ArticleFavorite> favQuery = new LambdaQueryWrapper<>();
        favQuery.eq(ArticleFavorite::getUserId, userId)
                .eq(ArticleFavorite::getCollectStatus, BusinessConstant.Collect_STATUS1);
        List<Long> favIds = articleFavoriteMapper.selectList(favQuery)
                .stream().map(ArticleFavorite::getArticleId).collect(Collectors.toList());
        if (favIds.isEmpty()) {
            return new PageResult<>(List.of(), 0L);
        }

        // 查这些文章，只查上架未删除的
        LambdaQueryWrapper<HealthArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(HealthArticle::getId, favIds)
                .eq(HealthArticle::getIsDeleted, BusinessConstant.isNotDelete)
                .eq(HealthArticle::getStatus, BusinessConstant.Article_Status_On)
                .orderByDesc(HealthArticle::getPublishingTime);

        Page<HealthArticle> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<HealthArticle> result = healthArticleMapper.selectPage(page, wrapper);

        // 收藏列表里全部标记为已收藏
        result.getRecords().forEach(a -> a.setIsFavorited(true));

        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public PageResult<HealthArticle> pageArticleAdmin(ArticlePageDTO dto) {
        LambdaQueryWrapper<HealthArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthArticle::getIsDeleted, BusinessConstant.isNotDelete);

        if (StringUtils.isNotBlank(dto.getCategory())) {
            wrapper.eq(HealthArticle::getCategory, dto.getCategory());
        }
        if (StringUtils.isNotBlank(dto.getKeyword())) {
            wrapper.like(HealthArticle::getTitle, dto.getKeyword());
        }
        // 管理端按传参筛选状态，不传则查全部
        if (dto.getStatus() != null) {
            wrapper.eq(HealthArticle::getStatus, dto.getStatus());
        }

        wrapper.orderByDesc(HealthArticle::getPublishingTime);

        Page<HealthArticle> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<HealthArticle> result = healthArticleMapper.selectPage(page, wrapper);

        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public HealthArticle getDetail(Long id) {
        HealthArticle article = healthArticleMapper.selectById(id);
        if (article == null || article.getIsDeleted().equals(BusinessConstant.isDelete)) {
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
        article.setViewCount(BusinessConstant.Article_View_Init);
        if (article.getStatus() == null) {
            article.setStatus(BusinessConstant.Article_Status_On);
        }
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        healthArticleMapper.insert(article);
        deleteArticleListCache();
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
        deleteArticleListCache();
    }

    @Override
    public void deleteArticle(Long id) {
        HealthArticle article = healthArticleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("资讯不存在");
        }
        if(Objects.equals(article.getStatus(), BusinessConstant.Article_Status_On)){
            throw new BusinessException("该资讯处于上架状态,不可删除！");
        }
        article.setIsDeleted(BusinessConstant.isDelete);
        article.setUpdateTime(LocalDateTime.now());
        healthArticleMapper.updateById(article);
        deleteArticleListCache();
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
        deleteArticleListCache();
    }

    @Override
    @Transactional
    public void updateFavoriteStatus(Long articleId, Integer status) {
        Long userId = UserInfoContext.getUserId();

        LambdaQueryWrapper<ArticleFavorite> query = new LambdaQueryWrapper<>();
        query.eq(ArticleFavorite::getUserId, userId)
                .eq(ArticleFavorite::getArticleId, articleId);
        ArticleFavorite favorite = articleFavoriteMapper.selectOne(query);

        if (favorite == null) {
            favorite = new ArticleFavorite();
            favorite.setUserId(userId);
            favorite.setArticleId(articleId);
            favorite.setCollectStatus(status);
            favorite.setCreateTime(LocalDateTime.now());
            favorite.setUpdateTime(LocalDateTime.now());
            articleFavoriteMapper.insert(favorite);
        } else {
            favorite.setCollectStatus(status);
            favorite.setUpdateTime(LocalDateTime.now());
            articleFavoriteMapper.updateById(favorite);
        }

        // 维护 ZSet 排行：收藏 +1，取消收藏 -1，归零移除
        String rankKey = favoritesRankKey();
        int delta = status.equals(BusinessConstant.Collect_STATUS1) ? 1 : -1;
        Double newScore = redisTemplate.opsForZSet()
                .incrementScore(rankKey, articleId, delta);
        if (newScore != null && newScore <= 0) {
            redisTemplate.opsForZSet().remove(rankKey, articleId);
        }
        redisTemplate.expire(rankKey, 2, TimeUnit.DAYS);
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



    /** 获取收藏量最高的 N 篇资讯 */
    @Override
    public List<HealthArticle> getTopFavorited(int limit) {
        return queryRank(favoritesRankKey(), limit, true);
    }

    /** 管理端查看某日排行（含收藏数） */
    @Override
    public List<HealthArticle> getAdminRank(String date, int limit) {
        String rankKey = FAVORITES_RANK_PREFIX + date;
        return queryRank(rankKey, limit, false);
    }

    /** 通用排行查询 */
    private List<HealthArticle> queryRank(String rankKey, int limit, boolean onlyOnline) {
        Set<ZSetOperations.TypedTuple<Object>> topTuples =
                redisTemplate.opsForZSet()
                        .reverseRangeWithScores(rankKey, 0, limit - 1);

        if (topTuples == null || topTuples.isEmpty()) {
            return List.of();
        }

        // 提取 articleId 和分数
        List<Long> ids = new ArrayList<>();
        Map<Long, Integer> scoreMap = new HashMap<>();
        for (ZSetOperations.TypedTuple<Object> t : topTuples) {
            Long id = ((Number) t.getValue()).longValue();
            ids.add(id);
            scoreMap.put(id, t.getScore() != null ? t.getScore().intValue() : 0);
        }

        // 批量查 DB
        List<HealthArticle> articles = healthArticleMapper.selectBatchIds(ids);
        if (onlyOnline) {
            articles.removeIf(a -> a.getIsDeleted().equals(BusinessConstant.isDelete)
                    || !a.getStatus().equals(BusinessConstant.Article_Status_On));
        }

        // 构建 ID→文章 映射，按 ZSet 顺序返回，附带分数
        Map<Long, HealthArticle> articleMap = new HashMap<>();
        for (HealthArticle a : articles) {
            articleMap.put(a.getId(), a);
        }
        List<HealthArticle> result = new ArrayList<>();
        for (Long id : ids) {
            HealthArticle a = articleMap.get(id);
            if (a != null) {
                a.setFavoriteCount(scoreMap.get(id));
                result.add(a);
            }
        }
        return result;
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

    /** 清除文章列表缓存 */
    private void deleteArticleListCache() {
        redisTemplate.delete(redisTemplate.keys(CACHE_PREFIX + "*"));
    }
}
