package com.chronicdisease.user.controller;

import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.user.domain.dto.HealthArchiveDTO;
import com.chronicdisease.user.domain.entity.HealthArchive;
import com.chronicdisease.user.domain.query.HealthArchiveQuery;
import com.chronicdisease.user.domain.vo.PatientBriefVO;
import com.chronicdisease.user.service.Impl.IHealthArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/archive")
@Slf4j
@Tag(name = "健康档案管理", description = "健康档案相关接口")
public class HealthArchiveController {

    @Autowired
    private IHealthArchiveService healthArchiveService;

    @OperationLog(module = "健康档案", description = "查询患者档案")
    @GetMapping("/my")
    @Operation(summary = "查询当前患者的健康档案")
    public Result<HealthArchive> getMyArchive() {
        HealthArchive result = healthArchiveService.getMyArchive();
        return Result.success(result);
    }

    @OperationLog(module = "健康档案", description = "新增健康档案")
    @PostMapping("/add")
    @Operation(summary = "新增健康档案")
    public Result<Void> addArchive(@Valid @RequestBody HealthArchiveDTO dto) {
        healthArchiveService.addArchive(dto);
        return Result.success();
    }

    @OperationLog(module = "健康档案", description = "编辑健康档案")
    @PostMapping("/edit")
    @Operation(summary = "编辑健康档案")
    public Result<Void> editArchive(@Valid @RequestBody HealthArchiveDTO dto) {
        healthArchiveService.editArchive(dto);
        return Result.success();
    }

    @OperationLog(module = "健康档案", description = "分页查询档案")
    @PostMapping("/page")
    @Operation(summary = "分页查询健康档案（管理员）")
    public Result<PageResult<HealthArchive>> page(@RequestBody HealthArchiveQuery query) {
        PageResult<HealthArchive> result = healthArchiveService.pageArchive(query);
        return Result.success(result);
    }

    @OperationLog(module = "健康档案", description = "删除健康档案")
    @PostMapping("/delete")
    @Operation(summary = "删除健康档案（管理员）")
    public Result<Void> delete(@RequestParam("id") Long id) {
        healthArchiveService.deleteArchive(id);
        return Result.success();
    }

    @PostMapping("/searchIds")
    @Operation(summary = "根据姓名查询用户ID")
    public Result<List<Long>> searchUserIds(@RequestParam("patientName") String patientName){
        List<Long> Ids = healthArchiveService.searchUserIds(patientName);
        return Result.success(Ids);
    }


    @GetMapping("/allPatientBriefs")
    @Operation(summary = "获取全部患者 userId做patientName映射")
    public Result<List<PatientBriefVO>> getAllPatientBriefs() {
        return Result.success(healthArchiveService.getAllPatientBriefs());
    }
    

}
