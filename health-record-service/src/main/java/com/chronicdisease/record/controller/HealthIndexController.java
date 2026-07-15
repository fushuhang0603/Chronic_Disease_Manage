package com.chronicdisease.record.controller;

import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.record.domain.dto.HealthIndexDTO;
import com.chronicdisease.record.domain.dto.HealthIndexPageDTO;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.record.domain.entity.HealthIndexRecord;
import com.chronicdisease.record.service.Impl.IHealthIndexService;
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

    @OperationLog(module = "健康指标", description = "获取指标图表数据")
    @GetMapping("/chart")
    @Operation(summary = "获取健康指标图表数据")
    public Result<Map<String, List<HealthIndexRecord>>> getChart(@RequestParam(value = "Days", defaultValue = "7") Integer Days) {
        Map<String, List<HealthIndexRecord>> map = healthIndexService.getChart(Days);
        return Result.success(map);
    }

    @OperationLog(module = "健康指标", description = "删除健康指标记录")
    @PostMapping("/delete")
    @Operation(summary = "删除健康指标记录")
    public Result<Void> delete(@RequestParam("id") Long id) {
        healthIndexService.deleteRecord(id);
        return Result.success();
    }
}
