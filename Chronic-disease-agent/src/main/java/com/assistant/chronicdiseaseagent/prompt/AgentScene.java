package com.assistant.chronicdiseaseagent.prompt;

/**
 * AI 交互场景
 */
public enum AgentScene {

    /** 自由对话 — 自然语言，简洁回复 */
    CHAT,

    /** 结构化分析 — 指标解读 + 趋势分析 + 风险评估 */
    ANALYZE,

    /** 患者健康小结 — 200字内摘要 */
    SUMMARIZE,

    /** 紧急筛查 — 识别危急症状，阻断并引导就医 */
    EMERGENCY_SCREENING
}
