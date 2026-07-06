package com.chronicdisease.user.controller;

import com.chronicdisease.common.result.Result;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.user.domain.dto.HealthArchiveDTO;
import com.chronicdisease.user.domain.entity.HealthArchive;
import com.chronicdisease.user.service.Impl.IHealthArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/archive")
@Slf4j
@Tag(name = "健康档案管理", description = "健康档案相关接口")
public class HealthArchiveController {

    @Autowired
    private IHealthArchiveService healthArchiveService;

    @GetMapping("/my")
    @Operation(summary = "查询当前患者的健康档案")
    public Result<HealthArchive> getMyArchive() {
        Long userId = UserInfoContext.getUserId();
        log.info("查询健康档案, userId={}", userId);
        HealthArchive result = healthArchiveService.getMyArchive(userId);
        return Result.success(result);
    }

    @PostMapping("/add")
    @Operation(summary = "新增健康档案")
    public Result<Void> addArchive(@Valid @RequestBody HealthArchiveDTO dto) {
        Long userId = UserInfoContext.getUserId();
        log.info("新增健康档案, userId={}, dto={}", userId, dto);
        healthArchiveService.addArchive(userId, dto);
        return Result.success();
    }

    @PostMapping("/edit")
    @Operation(summary = "编辑健康档案")
    public Result<Void> editArchive(@Valid @RequestBody HealthArchiveDTO dto) {
        Long userId = UserInfoContext.getUserId();
        log.info("编辑健康档案, userId={}, dto={}", userId, dto);
        healthArchiveService.editArchive(userId, dto);
        return Result.success();
    }
}
