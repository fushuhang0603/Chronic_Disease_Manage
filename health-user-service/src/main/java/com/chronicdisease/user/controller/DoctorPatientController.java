package com.chronicdisease.user.controller;

import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.user.domain.entity.DoctorProfile;
import com.chronicdisease.user.service.IDoctorPatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctor/patient")
@Slf4j
@Tag(name = "医生患者绑定", description = "患者端医生绑定相关接口")
public class DoctorPatientController {

    @Autowired
    private IDoctorPatientService doctorPatientService;

    @OperationLog(module = "医生绑定", description = "患者查询自己绑定的医生")
    @GetMapping("/my")
    @Operation(summary = "查询我的医生")
    public Result<DoctorProfile> my() {
        DoctorProfile result = doctorPatientService.getMyDoctor();
        return Result.success(result);
    }

    @OperationLog(module = "医生绑定", description = "患者绑定医生")
    @PostMapping("/bind")
    @Operation(summary = "绑定医生")
    public Result<Void> bind(@RequestParam("doctorId") Long doctorId) {
        doctorPatientService.bindDoctor(doctorId);
        return Result.success();
    }

    @OperationLog(module = "医生绑定", description = "患者解绑医生")
    @PostMapping("/unbind")
    @Operation(summary = "解绑医生")
    public Result<Void> unbind() {
        doctorPatientService.unbindDoctor();
        return Result.success();
    }
}
