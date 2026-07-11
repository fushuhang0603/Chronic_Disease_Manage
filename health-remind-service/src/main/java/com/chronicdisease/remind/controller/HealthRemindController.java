package com.chronicdisease.remind.controller;

import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.remind.domain.dto.HealthRemindDTO;
import com.chronicdisease.remind.domain.dto.HealthRemindPageDTO;
import com.chronicdisease.remind.domain.entity.HealthRemind;
import com.chronicdisease.remind.service.IHealthRemindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/remind")
@Slf4j
@Tag(name = "健康提醒管理", description = "健康提醒相关接口")
public class HealthRemindController {

    @Autowired
    private IHealthRemindService healthRemindService;

    @PostMapping("/add")
    @Operation(summary = "新增提醒")
    public Result<Void> add(@Valid @RequestBody HealthRemindDTO dto) {
        healthRemindService.addRemind(dto);
        return Result.success();
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询提醒")
    public Result<PageResult<HealthRemind>> page(@RequestBody HealthRemindPageDTO dto) {
        PageResult<HealthRemind> result = healthRemindService.pageRemind(dto);
        return Result.success(result);
    }

    @PostMapping("/read")
    @Operation(summary = "标记已读")
    public Result<Void> read(@RequestParam("id") Long id) {
        healthRemindService.readRemind(id);
        return Result.success();
    }

    @PostMapping("/complete")
    @Operation(summary = "标记已完成")
    public Result<Void> complete(@RequestParam("id") Long id) {
        healthRemindService.completeRemind(id);
        return Result.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除提醒")
    public Result<Void> delete(@RequestParam("id") Long id) {
        healthRemindService.deleteRemind(id);
        return Result.success();
    }

    @GetMapping("/upcoming")
    @Operation(summary = "获取最近的待提醒列表")
    public Result<List<HealthRemind>> upcoming() {
        List<HealthRemind> result = healthRemindService.upcomingRemind();
        return Result.success(result);
    }

}
