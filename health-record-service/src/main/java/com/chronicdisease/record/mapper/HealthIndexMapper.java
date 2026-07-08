package com.chronicdisease.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronicdisease.record.domain.entity.HealthIndexRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthIndexMapper extends BaseMapper<HealthIndexRecord> {
}
