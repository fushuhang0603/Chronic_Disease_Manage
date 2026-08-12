package com.assistant.chronicdiseaseagent.common.client;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * health-record-service Feign 客户端
 */
@FeignClient(name = "health-record-service")
public interface HealthRecordClient {


}
