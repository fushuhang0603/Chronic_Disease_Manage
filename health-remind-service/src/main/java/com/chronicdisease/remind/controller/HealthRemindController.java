package com.chronicdisease.remind.controller;

import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.remind.domain.dto.HealthRemindAdminPageDTO;
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

    @OperationLog(module = "提醒管理", description = "新增提醒")
    @PostMapping("/add")
    @Operation(summary = "新增提醒")
    public Result<Void> add(@Valid @RequestBody HealthRemindDTO dto) {
        healthRemindService.addRemind(dto);
        return Result.success();
    }

    @OperationLog(module = "提醒管理", description = "分页查询提醒")
    @PostMapping("/page")
    @Operation(summary = "分页查询提醒")
    public Result<PageResult<HealthRemind>> page(@RequestBody HealthRemindPageDTO dto) {
        PageResult<HealthRemind> result = healthRemindService.pageRemind(dto);
        return Result.success(result);
    }

    @OperationLog(module = "提醒管理", description = "更新提醒状态")
    @PostMapping("/updateStatus")
    @Operation(summary = "更新提醒状态")
    public Result<Void> updateStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        healthRemindService.updateStatus(id, status);
        return Result.success();
    }

    @OperationLog(module = "提醒管理", description = "删除提醒")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam("id") Long id) {
        healthRemindService.deleteRemind(id);
        return Result.success();
    }

    @OperationLog(module = "提醒管理", description = "管理端分页查全部提醒")
    @PostMapping("/pageAll")
    @Operation(summary = "管理端分页查询全部患者提醒")
    public Result<PageResult<HealthRemind>> pageAll(@RequestBody HealthRemindAdminPageDTO dto) {
        return Result.success(healthRemindService.pageAllRemind(dto));
    }

}

