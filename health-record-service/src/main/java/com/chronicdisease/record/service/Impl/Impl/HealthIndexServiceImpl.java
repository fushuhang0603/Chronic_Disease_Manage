package com.chronicdisease.record.service.Impl.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.UnitEnum;
import com.chronicdisease.record.domain.dto.HealthIndexDTO;
import com.chronicdisease.record.domain.dto.HealthIndexPageDTO;
import com.chronicdisease.record.domain.entity.HealthIndexRecord;
import com.chronicdisease.record.mapper.HealthIndexMapper;
import com.chronicdisease.record.service.Impl.IHealthIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HealthIndexServiceImpl extends ServiceImpl<HealthIndexMapper, HealthIndexRecord> implements IHealthIndexService {
    @Autowired
    private HealthIndexMapper healthIndexMapper;

    @Override
    public void addRecord(Long userId, HealthIndexDTO dto) {
        HealthIndexRecord record = new HealthIndexRecord();
        record.setUserId(userId);
        record.setIndexCode(dto.getIndexCode());
        record.setUnit(UnitEnum.getUnitByCode(dto.getIndexCode()));
        record.setIndexValue(dto.getIndexValue());
        record.setRecordTime(dto.getRecordTime() != null ? dto.getRecordTime() : LocalDateTime.now());
        record.setRemark(dto.getRemark());
        healthIndexMapper.insert(record);
    }

    @Override
    public IPage<HealthIndexRecord> pageRecords(Long userId, HealthIndexPageDTO dto) {
        LambdaQueryWrapper<HealthIndexRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthIndexRecord::getUserId, userId);
        if (dto.getIndexCode() != null && !dto.getIndexCode().isEmpty()) {
            wrapper.eq(HealthIndexRecord::getIndexCode, dto.getIndexCode());
        }
        if (dto.getStartTime() != null) {
            wrapper.ge(HealthIndexRecord::getRecordTime, dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            wrapper.le(HealthIndexRecord::getRecordTime, dto.getEndTime());
        }
        wrapper.orderByDesc(HealthIndexRecord::getRecordTime);
        Page<HealthIndexRecord> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        return healthIndexMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, List<HealthIndexRecord>> getChart(Long userId, Integer days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastTime = now.minusDays(days);
        LambdaQueryWrapper<HealthIndexRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthIndexRecord::getUserId, userId)
                .between(HealthIndexRecord::getRecordTime, pastTime, now)
                .orderByAsc(HealthIndexRecord::getRecordTime);
        List<HealthIndexRecord> records = healthIndexMapper.selectList(wrapper);
        Map<String, List<HealthIndexRecord>> map = records.stream().collect(Collectors.groupingBy(HealthIndexRecord::getIndexCode));
        return map;
    }
}
