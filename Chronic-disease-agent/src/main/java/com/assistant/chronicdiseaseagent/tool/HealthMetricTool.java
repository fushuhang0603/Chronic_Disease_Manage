package com.assistant.chronicdiseaseagent.tool;

import com.assistant.chronicdiseaseagent.common.Entity.IndexDict;
import com.assistant.chronicdiseaseagent.common.Entity.IndexDictQuery;
import com.assistant.chronicdiseaseagent.common.Entity.TrendPointVO;
import com.assistant.chronicdiseaseagent.common.client.HealthRecordClient;
import com.assistant.chronicdiseaseagent.common.client.UserServiceClient;
import com.assistant.chronicdiseaseagent.common.result.PageResult;
import com.assistant.chronicdiseaseagent.common.result.Result;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 健康指标趋势查询工具 — 让患者通过 AI 对话查询自己某项健康指标近期的变化趋势
 * 流程：① 查指标字典模糊匹配名称 → ② 查聚合趋势接口 → ③ 压缩为文本摘要返回
 */
@Component
public class HealthMetricTool {

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private HealthRecordClient healthRecordClient;

    @Tool(description = "查询患者本人某项健康指标近期的变化趋势。传入指标名称（如：血糖、血压、空腹血糖、糖化血红蛋白、尿酸、心率、体重、BMI、血脂等），返回该指标最近几天的日均值、最高、最低及测量次数，用于判断指标是否稳定或异常")
    public String queryMetricTrend(
            @ToolParam(description = "患者想查询的健康指标名称，例如：血糖、空腹血糖、血压、尿酸、心率、体重", required = true) String indexName,
            @ToolParam(description = "查询最近多少天的数据，默认7天", required = false) Integer days) {

        if (indexName == null || indexName.trim().isEmpty()) {
            return "请明确告知要查询的指标名称，例如：血糖、血压、尿酸等。";
        }
        if (days == null || days <= 0) {
            days = 7;
        }

        // 查询指标字典（仅指标类），用于名称映射
        List<IndexDict> indicators = loadIndicators();
        if (indicators.isEmpty()) {
            return "暂未获取到指标字典数据，请稍后重试。";
        }

        //模糊匹配指标名称 → 得到 indexCode + 正常范围
        List<IndexDict> matched = matchIndicators(indexName, indicators);
        if (matched.isEmpty()) {
            return "未找到与“" + indexName + "”匹配的指标。当前支持的指标包括："
                    + indicators.stream().map(IndexDict::getIndexName).collect(Collectors.joining("、"))
                    + "，请换一个更准确的名称再试。";
        }

        //查聚合趋势（按天粒度）
        List<String> indexCodes = matched.stream().map(IndexDict::getIndexCode).collect(Collectors.toList());
        Result<Map<String, List<TrendPointVO>>> trendResult =
                healthRecordClient.trend(days, "DAY", indexCodes);

        if (trendResult == null || trendResult.getData() == null) {
            return "趋势数据查询失败，请稍后重试。";
        }

        //压缩为文本摘要
        return buildSummary(matched, trendResult.getData(), days);
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

    /** 模糊匹配：精确匹配优先，其次为包含关系，返回得分最高的一组 */
    private List<IndexDict> matchIndicators(String input, List<IndexDict> indicators) {
        String key = input.trim().toLowerCase();
        List<IndexDict> matched = new ArrayList<>();
        int bestScore = 0;
        for (IndexDict d : indicators) {
            int score = matchScore(key, d);
            if (score > bestScore) {
                matched.clear();
                matched.add(d);
                bestScore = score;
            } else if (score > 0 && score == bestScore) {
                matched.add(d);
            }
        }
        return matched;
    }

    private int matchScore(String key, IndexDict d) {
        String name = d.getIndexName() == null ? "" : d.getIndexName().trim().toLowerCase();
        String code = d.getIndexCode() == null ? "" : d.getIndexCode().trim().toLowerCase();
        if (name.equals(key) || code.equals(key)) {
            return 2;
        }
        if (name.contains(key) || key.contains(name) || code.contains(key)) {
            return 1;
        }
        return 0;
    }

    private String buildSummary(List<IndexDict> matched, Map<String, List<TrendPointVO>> data, int days) {
        StringBuilder sb = new StringBuilder();
        sb.append("以下是患者近 ").append(days).append(" 天的指标趋势（按日均值汇总）：\n");

        for (IndexDict dict : matched) {
            String code = dict.getIndexCode();
            List<TrendPointVO> points = data.get(code);

            sb.append("\n【").append(dict.getIndexName()).append("】");
            if (dict.getMinValue() != null || dict.getMaxValue() != null) {
                sb.append("（正常范围 ");
                sb.append(dict.getMinValue() != null ? dict.getMinValue().stripTrailingZeros().toPlainString() : "-");
                sb.append(" ~ ");
                sb.append(dict.getMaxValue() != null ? dict.getMaxValue().stripTrailingZeros().toPlainString() : "-");
                sb.append("）");
            }
            sb.append("\n");

            if (points == null || points.isEmpty()) {
                sb.append("  该指标近 ").append(days).append(" 天暂无记录。\n");
                continue;
            }

            String unit = resolveUnit(points);
            for (TrendPointVO p : points) {
                sb.append("- ").append(p.getTimeLabel()).append("：均值 ")
                        .append(fmt(p.getAvgValue()));
                if (p.getMinValue() != null || p.getMaxValue() != null) {
                    sb.append("（最低 ").append(fmt(p.getMinValue()))
                            .append(" / 最高 ").append(fmt(p.getMaxValue())).append("）");
                }
                sb.append("，测量 ").append(p.getRecordCount()).append(" 次");
                if (unit != null && !unit.isEmpty()) {
                    sb.append("，单位 ").append(unit);
                }
                sb.append("\n");
            }
        }

        sb.append("\n请结合指标的正常范围和变化趋势，向患者简要解读：数值是否在正常范围内、是否波动较大、是否需要关注或复诊。");
        return sb.toString();
    }

    private String resolveUnit(List<TrendPointVO> points) {
        for (TrendPointVO p : points) {
            if (p.getDetails() != null) {
                for (TrendPointVO.DetailItem item : p.getDetails()) {
                    if (item.getUnit() != null && !item.getUnit().isEmpty()) {
                        return item.getUnit();
                    }
                }
            }
        }
        return null;
    }

    private String fmt(java.math.BigDecimal v) {
        return v == null ? "-" : v.stripTrailingZeros().toPlainString();
    }
}
