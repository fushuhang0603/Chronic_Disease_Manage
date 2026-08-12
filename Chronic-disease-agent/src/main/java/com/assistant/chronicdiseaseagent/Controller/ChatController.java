package com.assistant.chronicdiseaseagent.Controller;

import com.assistant.chronicdiseaseagent.advisor.MedicalSafetyAdvisor;
import com.assistant.chronicdiseaseagent.prompt.AgentRole;
import com.assistant.chronicdiseaseagent.prompt.AgentScene;
import com.assistant.chronicdiseaseagent.prompt.SystemPromptFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/agent")
public class ChatController {

    @Autowired
    private ChatClient chatClient;

    /** System Prompt 工厂 — 按角色/场景动态组装 */
    @Autowired
    private SystemPromptFactory promptFactory;

    /** 免责声明 — 追加到每次回复末尾 */
    private static final String DISCLAIMER =
            "\n\n---\n温馨提示：内容仅作健康科普参考，不具备临床诊疗作用，用药与病情诊治请遵从线下医生指导。";

    /**
     * 医生端 — 智能医助对话接口
     * DOCTOR + CHAT：以医生助手身份进行自由对话。
     */
    @GetMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> chat(@RequestParam String userInput) {
        String systemPrompt = promptFactory.build(AgentRole.DOCTOR, AgentScene.CHAT);
        return doChat(systemPrompt, userInput);
    }

    /**
     * 患者端 — 智能医助对话接口
     * PATIENT + CHAT：以患者健康助手身份对话，可调用医生推荐等工具。
     */
    @GetMapping(value = "/patient/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> patientChat(@RequestParam String userInput) {
        String systemPrompt = promptFactory.build(AgentRole.PATIENT, AgentScene.CHAT);
        return doChat(systemPrompt, userInput);
    }

    /** 统一对话执行 */
    private Flux<String> doChat(String systemPrompt, String userInput) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userInput)
                .advisors(new SimpleLoggerAdvisor(), new MedicalSafetyAdvisor())
                .stream()
                .content()
                .concatWith(Flux.just(DISCLAIMER));
    }
}
