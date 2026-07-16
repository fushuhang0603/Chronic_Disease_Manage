package com.chronicdisease.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronicdisease.article.domain.entity.HealthArticle;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthArticleMapper extends BaseMapper<HealthArticle> {
}
