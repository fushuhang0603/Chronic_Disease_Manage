package com.chronicdisease.record.feign;

import com.chronicdisease.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "health-user-service", path = "/archive")
@Component
public interface UserServiceFeign {

    @PostMapping("/searchIds")
    @Operation(summary = "根据姓名查询用户ID")
    Result<List<Long>> searchUserIds(@RequestParam("patientName") String patientName);


}
