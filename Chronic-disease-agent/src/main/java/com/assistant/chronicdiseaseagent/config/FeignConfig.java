package com.assistant.chronicdiseaseagent.config;

import com.assistant.chronicdiseaseagent.common.util.UserInfoContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor userInfoInterceptor() {
        return template -> {
            Long userId = UserInfoContext.getUserId();
            if (userId != null) {
                template.header("user-info", String.valueOf(userId));
            }
            String role = UserInfoContext.getRole();
            if (role != null) {
                template.header("user-role", role);
            }
        };
    }
}
