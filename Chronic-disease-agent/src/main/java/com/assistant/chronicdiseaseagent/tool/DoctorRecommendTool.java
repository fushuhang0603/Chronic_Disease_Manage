package com.assistant.chronicdiseaseagent.tool;

import com.assistant.chronicdiseaseagent.common.Entity.DoctorProfile;
import com.assistant.chronicdiseaseagent.common.Entity.DoctorProfileQuery;
import com.assistant.chronicdiseaseagent.common.client.UserServiceClient;
import com.assistant.chronicdiseaseagent.common.result.PageResult;
import com.assistant.chronicdiseaseagent.common.result.Result;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 医生推荐工具 — 根据疾病/科室/医院搜索匹配医生，供 AI 在对话中自动调用
 */
@Component
public class DoctorRecommendTool {

    @Autowired
    private UserServiceClient userServiceClient;

    @Tool(description = "根据患者疾病或症状推荐合适的医生。传入疾病名称（如糖尿病、高血压）、科室（如内分泌科、心内科）或医院名称进行筛选，返回匹配的医生列表及资历信息")
    public String recommendDoctors(
            @ToolParam(description = "患者疾病或症状，例如：糖尿病、高血压、冠心病", required = false) String condition,
            @ToolParam(description = "期望就诊的科室，例如：内分泌科、心内科", required = false) String department,
            @ToolParam(description = "期望就诊的医院名称", required = false) String hospital) {

        DoctorProfileQuery query = new DoctorProfileQuery();
        query.setDepartment(department);
        query.setHospital(hospital);
        query.setPageSize(5);

        Result<PageResult<DoctorProfile>> result = userServiceClient.page(query);

        if (result == null || result.getData() == null || result.getData().getRecords() == null) {
            return "未查询到匹配的医生信息，请稍后重试或调整搜索条件。";
        }

        List<DoctorProfile> doctors = result.getData().getRecords();
        if (doctors.isEmpty()) {
            return "当前条件下未找到匹配的医生，建议尝试放宽筛选条件（如不指定科室或医院）。";
        }

        return doctors.stream()
                .map(d -> String.format(
                        "- %s | %s | %s | %s | 擅长：%s",
                        d.getRealName(),
                        d.getTitle() != null ? d.getTitle() : "暂无职称",
                        d.getHospital(),
                        d.getDepartment(),
                        d.getSpecialty() != null ? d.getSpecialty() : "暂无"
                ))
                .collect(Collectors.joining("\n",
                        "为您找到以下 " + doctors.size() + " 位医生：\n",
                        "\n\n请根据患者的具体病情和位置，向患者推荐最合适的医生。"));
    }
}
