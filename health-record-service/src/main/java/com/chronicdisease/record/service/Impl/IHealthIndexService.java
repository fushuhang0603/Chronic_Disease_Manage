package com.chronicdisease.record.service.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.record.domain.dto.HealthIndexDTO;
import com.chronicdisease.record.domain.dto.HealthIndexPageDTO;
import com.chronicdisease.record.domain.entity.HealthIndexRecord;

import java.util.List;
import java.util.Map;

public interface IHealthIndexService extends IService<HealthIndexRecord> {

    void addRecord(Long userId, HealthIndexDTO dto);

    IPage<HealthIndexRecord> pageRecords(Long userId, HealthIndexPageDTO dto);

    Map<String, List<HealthIndexRecord>> getChart(Long userId, Integer days);
}
