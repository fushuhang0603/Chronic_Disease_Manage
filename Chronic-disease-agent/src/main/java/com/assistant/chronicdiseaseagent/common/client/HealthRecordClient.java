package com.assistant.chronicdiseaseagent.common.client;

import com.assistant.chronicdiseaseagent.common.Entity.HealthIndexPageDTO;
import com.assistant.chronicdiseaseagent.common.Entity.HealthIndexRecord;
import com.assistant.chronicdiseaseagent.common.result.PageResult;
import com.assistant.chronicdiseaseagent.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * health-record-service Feign 客户端
 */
@FeignClient(name = "health-record-service")
public interface HealthRecordClient {

    /**
     * 患者端分页查询指标原始记录（按时间范围，用于查某段时间/某月录入的记录）
     */
    @PostMapping("/index/page")
    Result<PageResult<HealthIndexRecord>> pageRecords(
            @RequestHeader("user-info") Long userId,
            @RequestBody HealthIndexPageDTO dto);
}
