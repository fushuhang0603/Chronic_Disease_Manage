package com.chronicdisease.record.feign;

import com.chronicdisease.common.result.Result;
import com.chronicdisease.record.domain.vo.UserBriefVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 医生患者绑定相关 Feign 调用
 */
@FeignClient(name = "health-user-service", path = "/doctor/patient")
@Component
public interface DoctorPatientFeign {

    @GetMapping("/myPatients")
    Result<List<Long>> getMyPatients();
}
