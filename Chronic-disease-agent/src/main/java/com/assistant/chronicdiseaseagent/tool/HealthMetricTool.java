package com.assistant.chronicdiseaseagent.tool;

import com.assistant.chronicdiseaseagent.common.Entity.HealthIndexPageDTO;
import com.assistant.chronicdiseaseagent.common.Entity.HealthIndexRecord;
import com.assistant.chronicdiseaseagent.common.Entity.IndexDict;
import com.assistant.chronicdiseaseagent.common.Entity.IndexDictQuery;
import com.assistant.chronicdiseaseagent.common.client.HealthRecordClient;
import com.assistant.chronicdiseaseagent.common.client.UserServiceClient;
import com.assistant.chronicdiseaseagent.common.result.PageResult;
import com.assistant.chronicdiseaseagent.common.result.Result;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 健康指标记录查询工具 — 让患者通过 AI 对话查询自己在指定时间范围内录入的指标记录
 * 流程：解析时间范围 → 查分页记录接口 → 拼装为文本摘要返回
 */
@Component
public class HealthMetricTool {

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private HealthRecordClient healthRecordClient;

    @Tool(description = "查询患者本人在指定时间范围内录入的健康指标原始记录（非趋势聚合）。传入开始时间和结束时间（如 2026-07-01 和 2026-07-31），返回该时间段内每一条记录的测量时间、指标名称、数值、单位及是否异常")
    public String queryMetricRecords(
            @ToolParam(description = "开始时间，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss", required = true) String startTime,
            @ToolParam(description = "结束时间，格式 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss", required = true) String endTime,
            ToolContext toolContext) {

        LocalDateTime start = parseTime(startTime, true);
        LocalDateTime end = parseTime(endTime, false);
        if (start == null || end == null) {
            return "时间格式不正确，请使用 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss。";
        }
        if (start.isAfter(end)) {
            return "开始时间不能晚于结束时间。";
        }

        Long userId = extractUserId(toolContext);
        if (userId == null) {
            return "未获取到您的身份信息，请重新登录后再试。";
        }

        HealthIndexPageDTO dto = new HealthIndexPageDTO();
        dto.setPageNum(1);
        dto.setPageSize(100);
        dto.setStartTime(start);
        dto.setEndTime(end);

        Result<PageResult<HealthIndexRecord>> result = healthRecordClient.pageRecords(userId, dto);
        if (result == null || result.getData() == null) {
            return "指标记录查询失败，请稍后重试。";
        }

        List<HealthIndexRecord> records = result.getData().getRecords();
        Long total = result.getData().getTotal();
        if (records == null || records.isEmpty()) {
            return "该时间段内暂无指标记录。";
        }

        Map<String, String> nameMap = loadIndicatorNameMap();

        StringBuilder sb = new StringBuilder();
        sb.append("查询到 ").append(total == null ? records.size() : total).append(" 条指标记录：\n");
        for (HealthIndexRecord r : records) {
            String name = nameMap.getOrDefault(r.getIndexCode(), r.getIndexCode());
            sb.append("- ").append(r.getRecordTime())
                    .append("：").append(name)
                    .append(" ").append(fmt(r.getIndexValue()));
            if (r.getUnit() != null && !r.getUnit().isEmpty()) {
                sb.append(" ").append(r.getUnit());
            }
            sb.append("（").append(abnormalLabel(r.getIsAbnormal())).append("）");
            if (r.getRemark() != null && !r.getRemark().isEmpty()) {
                sb.append("，备注：").append(r.getRemark());
            }
            sb.append("\n");
        }
        if (total != null && records.size() < total) {
            sb.append("（仅展示前 ").append(records.size()).append(" 条，共 ").append(total).append(" 条）\n");
        }
        sb.append("\n请在回复中逐条完整罗列以上所有记录，不要省略或只做总结。");
        return sb.toString();
    }

    /** 解析开始/结束时间：支持 yyyy-MM-dd HH:mm:ss 与 yyyy-MM-dd 两种格式 */
    private LocalDateTime parseTime(String value, boolean isStart) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String v = value.trim();
        try {
            return LocalDateTime.parse(v, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ignored) {
        }
        try {
            LocalDate date = LocalDate.parse(v, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return isStart ? date.atStartOfDay() : date.atTime(23, 59, 59);
        } catch (Exception ignored) {
        }
        return null;
    }

    /** 指标编码 → 指标名称 映射（用于记录展示时把 code 换成中文名） */
    private Map<String, String> loadIndicatorNameMap() {
        return loadIndicators().stream()
                .filter(d -> d.getIndexCode() != null && d.getIndexName() != null)
                .collect(Collectors.toMap(IndexDict::getIndexCode, IndexDict::getIndexName, (a, b) -> a));
    }

    /** 异常状态转中文：0-正常 1-偏高 2-偏低 */
    private String abnormalLabel(Integer isAbnormal) {
        if (isAbnormal == null) {
            return "未知";
        }
        switch (isAbnormal) {
            case 0:
                return "正常";
            case 1:
                return "偏高";
            case 2:
                return "偏低";
            default:
                return "未知";
        }
    }

    /** 从 ToolContext 中提取 userId（由 ChatController 在请求线程注入） */
    private Long extractUserId(ToolContext toolContext) {
        if (toolContext == null || toolContext.getContext() == null) {
            return null;
        }
        Object userId = toolContext.getContext().get("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return null;
    }

    /** 查询全部启用的指标类字典项 */
    private List<IndexDict> loadIndicators() {
        IndexDictQuery query = new IndexDictQuery();
        query.setTermType("indicator");
        query.setPageNum(1);
        query.setPageSize(100);
        Result<PageResult<IndexDict>> result = userServiceClient.page(query);
        if (result == null || result.getData() == null || result.getData().getRecords() == null) {
            return new ArrayList<>();
        }
        return result.getData().getRecords();
    }

    private String fmt(java.math.BigDecimal v) {
        return v == null ? "-" : v.stripTrailingZeros().toPlainString();
    }
}
