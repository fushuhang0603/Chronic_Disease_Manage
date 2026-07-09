package com.chronicdisease.record.service.Impl.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.constant.UnitEnum;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.record.domain.dto.HealthIndexDTO;
import com.chronicdisease.record.domain.dto.HealthIndexPageDTO;
import com.chronicdisease.record.domain.entity.HealthIndexRecord;
import com.chronicdisease.record.mapper.HealthIndexMapper;
import com.chronicdisease.record.service.Impl.IHealthIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HealthIndexServiceImpl extends ServiceImpl<HealthIndexMapper, HealthIndexRecord> implements IHealthIndexService {

    @Autowired
    private HealthIndexMapper healthIndexMapper;

    @Override
    public void addRecord(HealthIndexDTO dto) {
        Long userId = UserInfoContext.getUserId();
        if (dto.getRecordTime() == null) {
            throw new BusinessException("记录时间不能为空！");
        }
        LocalDateTime recordTime = dto.getRecordTime();
        HealthIndexRecord record = new HealthIndexRecord();
        record.setUserId(userId);
        record.setIndexCode(dto.getIndexCode());
        record.setUnit(UnitEnum.getUnitByCode(dto.getIndexCode()));
        record.setIndexValue(dto.getIndexValue());
        record.setRecordTime(recordTime);
        record.setRemark(dto.getRemark());
        healthIndexMapper.insert(record);
    }

    @Override
    public PageResult<HealthIndexRecord> pageRecords(HealthIndexPageDTO dto) {
        Long userId = UserInfoContext.getUserId();
        LambdaQueryWrapper<HealthIndexRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthIndexRecord::getUserId, userId);
        wrapper.eq(HealthIndexRecord::getIsDeleted, BusinessConstant.isNotDelete);
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
        Page<HealthIndexRecord> dbResult = healthIndexMapper.selectPage(page, wrapper);
        return new PageResult<>(dbResult.getRecords(), dbResult.getTotal());
    }

    @Override
    public Map<String, List<HealthIndexRecord>> getChart(Integer days) {
        Long userId = UserInfoContext.getUserId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastTime = now.minusDays(days);
        LambdaQueryWrapper<HealthIndexRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthIndexRecord::getUserId, userId)
                .eq(HealthIndexRecord::getIsDeleted, BusinessConstant.isNotDelete)
                .between(HealthIndexRecord::getRecordTime, pastTime, now)
                .orderByAsc(HealthIndexRecord::getRecordTime);
        List<HealthIndexRecord> records = healthIndexMapper.selectList(wrapper);
        return records.stream().collect(Collectors.groupingBy(HealthIndexRecord::getIndexCode));
    }

    @Override
    public void deleteRecord(Long id) {
        Long userId = UserInfoContext.getUserId();
        LambdaUpdateWrapper<HealthIndexRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HealthIndexRecord::getId, id)
                .eq(HealthIndexRecord::getUserId, userId)
                .set(HealthIndexRecord::getIsDeleted, BusinessConstant.isDelete);
        healthIndexMapper.update(updateWrapper);
        log.info("健康指标记录删除成功, id={}, userId={}", id, userId);
    }
}
