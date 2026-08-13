package com.assistant.chronicdiseaseagent.common.client;

import com.assistant.chronicdiseaseagent.common.Entity.TrendPointVO;
import com.assistant.chronicdiseaseagent.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * health-record-service Feign 客户端
 */
@FeignClient(name = "health-record-service")
public interface HealthRecordClient {

    /**
     * 患者端指标趋势聚合数据（日/周/月粒度）
     * key 为指标编码 indexCode
     */
    @GetMapping("/index/chart/trend")
    Result<Map<String, List<TrendPointVO>>> trend(
            @RequestParam(value = "days", defaultValue = "7") Integer days,
            @RequestParam(value = "granularity", defaultValue = "DAY") String granularity,
            @RequestParam(value = "indexCodes", required = false) List<String> indexCodes);
}
