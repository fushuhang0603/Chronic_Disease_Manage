package com.chronicdisease.record.feign;

import com.chronicdisease.common.result.Result;
import com.chronicdisease.record.domain.vo.PatientBriefVO;
import com.chronicdisease.record.domain.vo.UserBriefVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "health-user-service")
@Component
public interface UserServiceFeign {

    @GetMapping("/archive/allPatientBriefs")
    @Operation(summary = "获取患者简要信息，patientName可选，不传返回全部")
    Result<List<PatientBriefVO>> getAllPatientBriefs(@RequestParam(value = "patientName", required = false) String patientName);

    @GetMapping("/user/queryById")
    @Operation(summary = "根据ID查询用户信息")
    Result<UserBriefVO> queryById(@RequestParam("id") Long id);

    @GetMapping("/doctor/patient/myPatients")
    @Operation(summary = "医生端：获取我的所有绑定患者ID列表")
    Result<List<Long>> getMyPatients(@RequestParam("doctorId") Long doctorId);

}
