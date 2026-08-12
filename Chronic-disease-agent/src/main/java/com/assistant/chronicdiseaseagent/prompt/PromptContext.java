package com.assistant.chronicdiseaseagent.prompt;

import lombok.Builder;
import lombok.Data;

/**
 * Prompt 组装上下文 — 携带角色、场景、患者信息、业务数据
 */
@Data
@Builder
public class PromptContext {

    /** AI 角色 */
    private AgentRole role;

    /** 交互场景 */
    private AgentScene scene;

    /** 患者基本信息（可选，后续 Feign 查库注入） */
    private PatientInfo patient;

    /** 前端传入的业务数据（JSON），如指标列表、用药记录 */
    private String extData;
}

@Data
@Builder
class PatientInfo {
    private String name;
    private Integer age;
    private String gender;
    /** 慢病病种，如 "2型糖尿病,高血压" */
    private String conditions;
}
