package com.assistant.chronicdiseaseagent.common.client;

import com.assistant.chronicdiseaseagent.common.Entity.DoctorProfile;
import com.assistant.chronicdiseaseagent.common.Entity.DoctorProfileQuery;
import com.assistant.chronicdiseaseagent.common.Entity.IndexDict;
import com.assistant.chronicdiseaseagent.common.Entity.IndexDictQuery;
import com.assistant.chronicdiseaseagent.common.result.PageResult;
import com.assistant.chronicdiseaseagent.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "health-user-service")
public interface UserServiceClient {

    @PostMapping("/doctor/profile/page")
    Result<PageResult<DoctorProfile>> page(@RequestBody DoctorProfileQuery query);

    @PostMapping("/dict/page")
    Result<PageResult<IndexDict>> page(@RequestBody IndexDictQuery query);
}
