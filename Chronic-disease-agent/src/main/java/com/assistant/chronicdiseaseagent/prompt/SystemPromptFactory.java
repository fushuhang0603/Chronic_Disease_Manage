package com.assistant.chronicdiseaseagent.prompt;

import com.assistant.chronicdiseaseagent.prompt.template.DoctorTemplates;
import com.assistant.chronicdiseaseagent.prompt.template.PatientTemplates;
import com.assistant.chronicdiseaseagent.prompt.template.SafetyRules;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * System Prompt 工厂 — 按角色 + 场景动态组装 System Prompt
 *
 * <p>组装顺序（4层）：</p>
 * <pre>
 * 1. SafetyRules        — 安全规则（分层，按场景选择）
 * 2. RoleTemplate       — 角色模板（身份 + 能力 + 场景输出格式 + 语气）
 * 3. PatientContext     — 患者上下文（有则拼接）
 * 4. ExtData            — 前端传入的业务数据（有则拼接）
 * </pre>
 *
 * <p>缓存策略：按 role + scene 做 key，避免重复组装</p>
 */
@Component
public class SystemPromptFactory {

    /** 最大字符上限，超出则裁剪 */
    private static final int MAX_PROMPT_CHARS = 3000;

    /** Prompt 缓存 */
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    /**
     * 构建 System Prompt
     *
     * @param ctx 上下文（角色 + 场景 + 患者信息）
     * @return 组装好的 System Prompt 字符串
     */
    public String build(PromptContext ctx) {
        AgentRole role = ctx.getRole();
        AgentScene scene = ctx.getScene();

        String cacheKey = role + ":" + scene;
        return cache.computeIfAbsent(cacheKey, k -> assemble(ctx));
    }

    /**
     * 核心组装逻辑
     */
    private String assemble(PromptContext ctx) {
        AgentRole role = ctx.getRole();
        AgentScene scene = ctx.getScene();

        StringBuilder prompt = new StringBuilder();

        // Layer 1: 安全规则（分层）
        prompt.append(SafetyRules.get(scene)).append("\n\n");

        // Layer 2: 角色模板（身份 + 能力 + 场景输出格式 + 语气）
        Map<AgentScene, String> templates = (role == AgentRole.DOCTOR)
                ? DoctorTemplates.SCENE_TEMPLATES
                : PatientTemplates.SCENE_TEMPLATES;
        prompt.append(templates.get(scene)).append("\n\n");

        // Layer 3: 患者上下文（有则拼接）
        if (ctx.getPatient() != null && ctx.getPatient().getName() != null) {
            prompt.append("【当前患者信息】\n");
            prompt.append("姓名: ").append(ctx.getPatient().getName());
            prompt.append("，年龄: ").append(ctx.getPatient().getAge()).append("岁");
            if (ctx.getPatient().getGender() != null) {
                prompt.append("，性别: ").append(ctx.getPatient().getGender());
            }
            if (ctx.getPatient().getConditions() != null) {
                prompt.append("，慢病: ").append(ctx.getPatient().getConditions());
            }
            prompt.append("\n\n");
        }

        // Layer 4: 业务数据（前端传入）
        if (ctx.getExtData() != null && !ctx.getExtData().isBlank()) {
            prompt.append("【患者数据】\n").append(ctx.getExtData()).append("\n\n");
        }

        // Token 预算裁剪
        String result = prompt.toString();
        if (result.length() > MAX_PROMPT_CHARS) {
            return result.substring(0, MAX_PROMPT_CHARS);
        }
        return result;
    }

    /**
     * 简化的便捷方法 — 仅指定角色和场景，无患者上下文
     */
    public String build(AgentRole role, AgentScene scene) {
        return build(PromptContext.builder()
                .role(role)
                .scene(scene)
                .build());
    }
}
