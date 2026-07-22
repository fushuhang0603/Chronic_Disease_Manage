package com.chronicdisease.record.service.Impl.Impl;

import cn.hutool.core.collection.CollUtil;
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
import com.chronicdisease.record.domain.vo.DailyAggregation;
import com.chronicdisease.record.domain.vo.PatientBriefVO;
import com.chronicdisease.record.domain.vo.TrendPointVO;
import com.chronicdisease.record.feign.UserServiceFeign;
import com.chronicdisease.record.mapper.HealthIndexMapper;
import com.chronicdisease.record.service.Impl.IHealthIndexService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HealthIndexServiceImpl extends ServiceImpl<HealthIndexMapper, HealthIndexRecord> implements IHealthIndexService {

    @Autowired
    private HealthIndexMapper healthIndexMapper;
    @Autowired
    private UserServiceFeign userServiceFeign;


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
    public void deleteRecord(Long id) {
        Long userId = UserInfoContext.getUserId();
        LambdaUpdateWrapper<HealthIndexRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HealthIndexRecord::getId, id)
                .eq(HealthIndexRecord::getUserId, userId)
                .set(HealthIndexRecord::getIsDeleted, BusinessConstant.isDelete);
        healthIndexMapper.update(updateWrapper);
        log.info("健康指标记录删除成功, id={}, userId={}", id, userId);
    }

    @Override
    public PageResult<HealthIndexRecord> pageRecordsByPatientName(String patientName, Integer pageNum, Integer pageSize, String indexCode) {
        Map<Long, String> nameMap = new HashMap<>();
        try {
            List<PatientBriefVO> briefs = userServiceFeign.getAllPatientBriefs(patientName).getData();
            if (CollUtil.isNotEmpty(briefs)) {
                nameMap = briefs.stream()
                        .collect(Collectors.toMap(PatientBriefVO::getUserId, PatientBriefVO::getPatientName, (a, b) -> a));
            }
        } catch (Exception e) {
            //降级处理
            log.warn("获取患者信息失败, patientName={}", patientName, e);
        }

        LambdaQueryWrapper<HealthIndexRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthIndexRecord::getIsDeleted, BusinessConstant.isNotDelete);
        if (StringUtils.isNotBlank(patientName)) {
            if (CollUtil.isEmpty(nameMap)) {
                return new PageResult<>();
            }
            wrapper.in(HealthIndexRecord::getUserId, nameMap.keySet());
        }

        if (StringUtils.isNotBlank(indexCode)) {
            wrapper.eq(HealthIndexRecord::getIndexCode, indexCode);
        }
        wrapper.orderByDesc(HealthIndexRecord::getRecordTime);

        Page<HealthIndexRecord> page = new Page<>(pageNum, pageSize);
        Page<HealthIndexRecord> result = healthIndexMapper.selectPage(page, wrapper);

        // 回填患者姓名
        List<HealthIndexRecord> records = result.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            Map<Long, String> finalNameMap = nameMap;
            records.forEach(r -> r.setPatientName(finalNameMap.getOrDefault(r.getUserId(), "-")));
        }

        return new PageResult<>(records, result.getTotal());
    }



    @Override
    public Map<String, List<TrendPointVO>> getTrend(Integer days, String granularity, List<String> indexCodes) {
        Long userId = UserInfoContext.getUserId();
        return buildTrendResult(userId, days, granularity, indexCodes);
    }

    @Override
    public Map<String, List<TrendPointVO>> getAdminTrend(String patientName, Integer days, String granularity, List<String> indexCodes) {
        // 通过 Feign 查 userId，绕开前端 JavaScript 雪花 ID 精度丢失问题
        Long userId = findUserIdByPatientName(patientName);
        if (userId == null) {
            return Collections.emptyMap();
        }
        return buildTrendResult(userId, days, granularity, indexCodes);
    }

    /**
     * 通过 patientName 查询 userId
     */
    private Long findUserIdByPatientName(String patientName) {
        try {
            List<PatientBriefVO> briefs = userServiceFeign.getAllPatientBriefs(patientName).getData();
            if (CollUtil.isNotEmpty(briefs)) {
                return briefs.get(0).getUserId();
            }
        } catch (Exception e) {
            log.warn("Feign 查询患者 userId 失败, patientName={}", patientName, e);
        }
        return null;
    }

    /**
     * 趋势聚合核心逻辑：
     * 1. SQL 按天聚合 → 得到每日 avFamilyg/max/min
     * 2. Java 层按 granularity 做周/月二次汇总
     * 3. DAY 粒度时附加当日原始明细记录
     */
    private Map<String, List<TrendPointVO>> buildTrendResult(Long userId, Integer days, String granularity, List<String> indexCodes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastTime = now.minusDays(days);

        // Step 1: SQL 日聚合
        List<DailyAggregation> dayList = healthIndexMapper.dailyAggregation(userId, pastTime, now, indexCodes);
        if (CollUtil.isEmpty(dayList)) {
            return Collections.emptyMap();
        }

        // 按 indexCode 分组
        Map<String, List<DailyAggregation>> codeGrouped = dayList.stream()
                .collect(Collectors.groupingBy(DailyAggregation::getIndexCode));

        String gran = (granularity == null) ? "DAY" : granularity.toUpperCase();
        Map<String, List<TrendPointVO>> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<DailyAggregation>> entry : codeGrouped.entrySet()) {
            String code = entry.getKey();
            List<DailyAggregation> dailyList = entry.getValue();

            List<TrendPointVO> points;
            switch (gran) {
                case "WEEK":
                    points = rollupByWeek(dailyList);
                    break;
                case "MONTH":
                    points = rollupByMonth(dailyList);
                    break;
                default:
                    points = buildDayPoints(userId, dailyList, pastTime, now);
                    break;
            }
            result.put(code, points);
        }

        return result;
    }

    /**
     * DAY 粒度：每日一个 TrendPointVO，附明细列表
     */
    private List<TrendPointVO> buildDayPoints(Long userId, List<DailyAggregation> dailyList,
                                               LocalDateTime startTime, LocalDateTime endTime) {
        // 查询原始记录用于明细
        List<HealthIndexRecord> rawRecords = queryRawRecords(userId, startTime, endTime,
                dailyList.stream().map(DailyAggregation::getIndexCode).distinct().collect(Collectors.toList()));
        Map<LocalDate, List<HealthIndexRecord>> rawByDate = rawRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getRecordTime().toLocalDate()));

        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("MM-dd");

        return dailyList.stream().map(d -> {
            TrendPointVO vo = new TrendPointVO();
            LocalDate date = d.getRecordDate().toLocalDate();
            vo.setTimeLabel(date.format(dayFmt));
            vo.setAvgValue(d.getAvgValue());
            vo.setMaxValue(d.getMaxValue());
            vo.setMinValue(d.getMinValue());
            vo.setRecordCount(d.getRecordCount());

            // 附加当日明细
            List<HealthIndexRecord> dayRecords = rawByDate.getOrDefault(date, Collections.emptyList());
            if (CollUtil.isNotEmpty(dayRecords)) {
                vo.setDetails(dayRecords.stream().map(r -> {
                    TrendPointVO.DetailItem item = new TrendPointVO.DetailItem();
                    item.setId(r.getId());
                    item.setIndexValue(r.getIndexValue());
                    item.setUnit(r.getUnit());
                    item.setRecordTime(r.getRecordTime());
                    return item;
                }).collect(Collectors.toList()));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * WEEK 粒度：按 ISO 周分组，汇总周均值/峰值/总次数
     */
    private List<TrendPointVO> rollupByWeek(List<DailyAggregation> dailyList) {
        WeekFields wf = WeekFields.of(Locale.getDefault());
        Map<Integer, List<DailyAggregation>> weekGrouped = dailyList.stream()
                .collect(Collectors.groupingBy(d -> d.getRecordDate().toLocalDate().get(wf.weekOfYear())));

        return weekGrouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    List<DailyAggregation> weekData = e.getValue();
                    TrendPointVO vo = new TrendPointVO();
                    vo.setTimeLabel("第" + e.getKey() + "周");
                    vo.setAvgValue(avgOfAvgs(weekData));
                    vo.setMaxValue(maxOfMaxs(weekData));
                    vo.setMinValue(minOfMins(weekData));
                    vo.setRecordCount(weekData.stream().mapToInt(DailyAggregation::getRecordCount).sum());
                    return vo;
                }).collect(Collectors.toList());
    }

    /**
     * MONTH 粒度：按年月分组
     */
    private List<TrendPointVO> rollupByMonth(List<DailyAggregation> dailyList) {
        Map<YearMonth, List<DailyAggregation>> monthGrouped = dailyList.stream()
                .collect(Collectors.groupingBy(d -> YearMonth.from(d.getRecordDate().toLocalDate())));

        return monthGrouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    List<DailyAggregation> monthData = e.getValue();
                    TrendPointVO vo = new TrendPointVO();
                    vo.setTimeLabel(e.getKey().getMonthValue() + "月");
                    vo.setAvgValue(avgOfAvgs(monthData));
                    vo.setMaxValue(maxOfMaxs(monthData));
                    vo.setMinValue(minOfMins(monthData));
                    vo.setRecordCount(monthData.stream().mapToInt(DailyAggregation::getRecordCount).sum());
                    return vo;
                }).collect(Collectors.toList());
    }

    private BigDecimal avgOfAvgs(List<DailyAggregation> list) {
        return list.stream().map(DailyAggregation::getAvgValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal maxOfMaxs(List<DailyAggregation> list) {
        return list.stream().map(DailyAggregation::getMaxValue).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    private BigDecimal minOfMins(List<DailyAggregation> list) {
        return list.stream().map(DailyAggregation::getMinValue).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    /**
     * 查询指定时间范围内的原始指标记录（用于 DAY 粒度的明细展开）
     */
    private List<HealthIndexRecord> queryRawRecords(Long userId, LocalDateTime startTime, LocalDateTime endTime,
                                                     List<String> indexCodes) {
        LambdaQueryWrapper<HealthIndexRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthIndexRecord::getUserId, userId)
                .eq(HealthIndexRecord::getIsDeleted, BusinessConstant.isNotDelete)
                .between(HealthIndexRecord::getRecordTime, startTime, endTime);
        if (CollUtil.isNotEmpty(indexCodes)) {
            wrapper.in(HealthIndexRecord::getIndexCode, indexCodes);
        }
        wrapper.orderByAsc(HealthIndexRecord::getRecordTime);
        return healthIndexMapper.selectList(wrapper);
    }
}
