package com.chronicdisease.record.controller;

import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.record.domain.dto.HealthIndexDTO;
import com.chronicdisease.record.domain.dto.HealthIndexPageDTO;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.record.domain.entity.HealthIndexRecord;
import com.chronicdisease.record.domain.vo.TrendPointVO;
import com.chronicdisease.record.service.IHealthIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/index")
@Slf4j
@Tag(name = "健康指标管理", description = "健康指标记录相关接口")
public class HealthIndexController {

    @Autowired
    private IHealthIndexService healthIndexService;

    @OperationLog(module = "健康指标", description = "录入健康指标")
    @PostMapping("/add")
    @Operation(summary = "录入健康指标记录")
    public Result<Void> add(@Valid @RequestBody HealthIndexDTO dto) {
        healthIndexService.addRecord(dto);
        return Result.success();
    }

    @OperationLog(module = "健康指标", description = "分页查询健康指标")
    @PostMapping("/page")
    @Operation(summary = "分页查询健康指标记录")
    public Result<PageResult<HealthIndexRecord>> page(@RequestBody HealthIndexPageDTO dto) {
        return Result.success(healthIndexService.pageRecords(dto));
    }

    @OperationLog(module = "健康指标", description = "删除健康指标记录")
    @PostMapping("/delete")
    @Operation(summary = "删除健康指标记录")
    public Result<Void> delete(@RequestParam("id") Long id) {
        healthIndexService.deleteRecord(id);
        return Result.success();
    }


    @OperationLog(module = "数据监测", description = "管理端查看指定患者指标记录")
    @GetMapping("/admin/records")
    @Operation(summary = "管理端查看指定患者指标记录")
    public Result<PageResult<HealthIndexRecord>> adminRecords(
            @RequestParam (value = "patientName",required = false)String patientName,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize,
            @RequestParam(value = "indexCode", required = false) String indexCode) {

        PageResult<HealthIndexRecord> pageResult = healthIndexService.pageRecordsByPatientName(patientName, pageNum, pageSize, indexCode);

        return Result.success(pageResult);
    }


    @OperationLog(module = "健康指标", description = "患者端获取指标趋势聚合数据")
    @GetMapping("/chart/trend")
    @Operation(summary = "患者端获取指标趋势聚合数据（日/周/月粒度）")
    public Result<Map<String, List<TrendPointVO>>> trend(
            @RequestParam(value = "days", defaultValue = "7") Integer days,
            @RequestParam(value = "granularity", defaultValue = "DAY") String granularity,
            @RequestParam(value = "indexCodes", required = false) List<String> indexCodes) {
        return Result.success(healthIndexService.getTrend(days, granularity, indexCodes));
    }

    @OperationLog(module = "数据监测", description = "管理端获取指定患者指标趋势聚合数据")
    @GetMapping("/admin/trend")
    @Operation(summary = "管理端获取指定患者指标趋势聚合数据（日/周/月粒度）")
    public Result<Map<String, List<TrendPointVO>>> adminTrend(
            @RequestParam(value = "patientName") String patientName,
            @RequestParam(value = "days", defaultValue = "30") Integer days,
            @RequestParam(value = "granularity", defaultValue = "DAY") String granularity,
            @RequestParam(value = "indexCodes", required = false) List<String> indexCodes) {
        return Result.success(healthIndexService.getAdminTrend(patientName, days, granularity, indexCodes));
    }
}
