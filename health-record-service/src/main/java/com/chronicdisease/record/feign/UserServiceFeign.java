package com.chronicdisease.record.feign;

import com.chronicdisease.common.result.Result;
import com.chronicdisease.record.domain.vo.PatientBriefVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "health-user-service", path = "/archive")
@Component
public interface UserServiceFeign {

    @GetMapping("/allPatientBriefs")
    @Operation(summary = "获取患者简要信息，patientName可选，不传返回全部")
    Result<List<PatientBriefVO>> getAllPatientBriefs(@RequestParam(value = "patientName", required = false) String patientName);

}
