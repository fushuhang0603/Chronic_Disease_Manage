package com.chronicdisease.user.controller;

import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.user.domain.dto.DoctorProfileDTO;
import com.chronicdisease.user.domain.entity.DoctorProfile;
import com.chronicdisease.user.domain.entity.User;
import com.chronicdisease.user.domain.query.DoctorProfileQuery;
import com.chronicdisease.user.service.IDoctorProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctor")
@Slf4j
@Tag(name = "医生管理", description = "医生资历与绑定相关接口")
public class DoctorProfileController {

    @Autowired
    private IDoctorProfileService doctorProfileService;

    @OperationLog(module = "医生管理", description = "分页查询医生资历")
    @PostMapping("/profile/page")
    @Operation(summary = "分页查询医生资历")
    public Result<PageResult<DoctorProfile>> page(@RequestBody DoctorProfileQuery query) {
        PageResult<DoctorProfile> result = doctorProfileService.getPage(query);
        return Result.success(result);
    }

    @OperationLog(module = "医生管理", description = "新增医生资历")
    @PostMapping("/profile/add")
    @Operation(summary = "新增医生资历")
    public Result<Void> add(@Valid @RequestBody DoctorProfileDTO dto) {
        doctorProfileService.addProfile(dto);
        return Result.success();
    }

    @OperationLog(module = "医生管理", description = "编辑医生资历")
    @PostMapping("/profile/edit")
    @Operation(summary = "编辑医生资历")
    public Result<Void> edit(@Valid @RequestBody DoctorProfileDTO dto) {
        doctorProfileService.editProfile(dto);
        return Result.success();
    }

    @OperationLog(module = "医生管理", description = "根据ID查询医生资历")
    @GetMapping("/profile/queryById")
    @Operation(summary = "根据ID查询医生资历")
    public Result<DoctorProfile> queryById(@RequestParam("id") Long id) {
        DoctorProfile result = doctorProfileService.queryById(id);
        return Result.success(result);
    }

    @OperationLog(module = "医生管理", description = "删除医生资历")
    @PostMapping("/profile/delete")
    @Operation(summary = "删除医生资历")
    public Result<Void> delete(@RequestParam("id") Long id) {
        doctorProfileService.deleteById(id);
        return Result.success();
    }

    @OperationLog(module = "医生管理", description = "获取未建资历的医生用户列表")
    @GetMapping("/user/list")
    @Operation(summary = "获取未建资历的医生用户列表")
    public Result<List<User>> getUserList() {
        List<User> result = doctorProfileService.getDoctorUserList();
        return Result.success(result);
    }

    @GetMapping("/profile/list")
    @Operation(summary = "患者端获取全部医生资历列表")
    public Result<List<DoctorProfile>> getDoctorList() {
        List<DoctorProfile> result = doctorProfileService.getDoctorList();
        return Result.success(result);
    }
}
