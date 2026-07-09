package com.chronicdisease.record.controller;

import com.chronicdisease.common.result.Result;
import com.chronicdisease.common.util.UserInfoContext;
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

    @PostMapping("/add")
    @Operation(summary = "录入健康指标记录")
    public Result<Void> add(@Valid @RequestBody HealthIndexDTO dto) {
        log.info("录入健康指标, indexCode={}, value={}", dto.getIndexCode(), dto.getIndexValue());
        healthIndexService.addRecord(dto);
        return Result.success();
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询健康指标记录")
    public Result<PageResult<HealthIndexRecord>> page(@RequestBody HealthIndexPageDTO dto) {
        Long userId = UserInfoContext.getUserId();
        log.info("分页查询健康指标, userId={}, indexCode={}, pageNum={}, pageSize={}",
                userId, dto.getIndexCode(), dto.getPageNum(), dto.getPageSize());
        return Result.success(healthIndexService.pageRecords(dto));
    }

    @GetMapping("/chart")
    @Operation(summary = "获取健康指标图表数据")
    public Result<Map<String, List<HealthIndexRecord>>> getChart(@RequestParam(value = "Days", defaultValue = "7") Integer Days) {
        Long userId = UserInfoContext.getUserId();
        log.info("获取健康指标图表数据");
        Map<String, List<HealthIndexRecord>> map = healthIndexService.getChart(Days);
        return Result.success(map);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除健康指标记录")
    public Result<Void> delete(@RequestParam("id") Long id) {
        log.info("删除健康指标记录, id={}", id);
        healthIndexService.deleteRecord(id);
        return Result.success();
    }
}
