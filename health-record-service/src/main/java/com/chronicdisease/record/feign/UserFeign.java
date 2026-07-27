package com.chronicdisease.record.feign;

import com.chronicdisease.common.result.Result;
import com.chronicdisease.record.domain.vo.PatientBriefVO;
import com.chronicdisease.record.domain.vo.UserBriefVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * user-service 用户查询相关 Feign 调用
 */
@FeignClient(name = "health-user-service", path = "/user")
@Component
public interface UserFeign {

    @GetMapping("/queryById")
    Result<UserBriefVO> queryById(@RequestParam("id") Long id);
}
