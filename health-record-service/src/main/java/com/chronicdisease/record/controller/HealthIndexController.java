package com.chronicdisease.record.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.record.domain.dto.HealthIndexDTO;
import com.chronicdisease.record.domain.dto.HealthIndexPageDTO;
import com.chronicdisease.record.domain.entity.HealthIndexRecord;
import com.chronicdisease.record.service.Impl.Impl.HealthIndexServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/index")
@Slf4j
@Tag(name = "健康指标管理", description = "健康指标记录相关接口")
public class HealthIndexController {

    @Autowired
    private HealthIndexServiceImpl healthIndexService;

    @PostMapping("/add")
    @Operation(summary = "录入健康指标记录")
    public Result<Void> add(@Valid @RequestBody HealthIndexDTO dto) {
        Long userId = UserInfoContext.getUserId();
        log.info("录入健康指标, userId={}, indexCode={}, value={}", userId, dto.getIndexCode(), dto.getIndexValue());
        healthIndexService.addRecord(userId, dto);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询健康指标记录")
    public Result<IPage<HealthIndexRecord>> page(HealthIndexPageDTO dto) {
        Long userId = UserInfoContext.getUserId();
        log.info("分页查询健康指标, userId={}, indexCode={}, pageNum={}, pageSize={}",
                userId, dto.getIndexCode(), dto.getPageNum(), dto.getPageSize());
        return Result.success(healthIndexService.pageRecords(userId, dto));
    }

    @GetMapping("/chart")
    @Operation(summary = "获取健康指标图表数据")
    public Result<Map<String, List<HealthIndexRecord>>> getChart( @RequestParam("Days") Integer Days){
        Long userId = UserInfoContext.getUserId();
        log.info("获取健康指标图表数据");
        Map<String, List<HealthIndexRecord>> chartData = healthIndexService.getChart(userId, Days);
        return null;
    }
}
