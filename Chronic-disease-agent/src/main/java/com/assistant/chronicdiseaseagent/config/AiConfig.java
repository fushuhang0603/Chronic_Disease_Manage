package com.assistant.chronicdiseaseagent.config;

import com.assistant.chronicdiseaseagent.tool.DoctorRecommendTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(OpenAiChatModel model, DoctorRecommendTool doctorRecommendTool) {
        return ChatClient.builder(model)
                .defaultTools(doctorRecommendTool)
                .build();
    }
}
