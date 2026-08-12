package com.assistant.chronicdiseaseagent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

/**
 * 医疗安全 Advisor — 预留扩展点
 * <p>
 * 当前安全约束由 Prompt 层 {@code SafetyRules} 处理（AI 自身拒绝违规请求），
 * 免责声明由 Controller 层 {@code concatWith(DISCLAIMER)} 追加。
 * 此处预留给后续需要代码级硬阻断的场景（如鉴权、限流、外部审核回调等）。
 */
public class MedicalSafetyAdvisor implements BaseAdvisor {

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
        // 预留：后续可在此处添加鉴权、限流等前置拦截
        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain advisorChain) {
        // 预留：后续可在此处添加响应审核、日志审计等后置处理
        return response;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
