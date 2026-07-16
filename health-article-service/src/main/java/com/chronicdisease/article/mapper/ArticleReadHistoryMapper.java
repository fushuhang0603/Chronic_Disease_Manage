package com.chronicdisease.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronicdisease.article.domain.entity.ArticleReadHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface ArticleReadHistoryMapper extends BaseMapper<ArticleReadHistory> {

    /** 查询该用户今天是否已读过该文章 */
    @Select("SELECT read_time FROM article_read_history WHERE user_id = #{userId} AND article_id = #{articleId} AND is_deleted = 0 ORDER BY read_time DESC LIMIT 1")
    LocalDateTime getLastReadTime(Long userId, Long articleId);
}
