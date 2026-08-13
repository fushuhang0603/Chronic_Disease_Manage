package com.assistant.chronicdiseaseagent.config;

import com.assistant.chronicdiseaseagent.common.handler.UserInfoInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 — 注册用户上下文拦截器
 * <p>
 * 网关鉴权后会把 userId/role 写入请求头 user-info / user-role，
 * 拦截器在请求进入 Controller 前将其解析到 {@code UserInfoContext}（ThreadLocal）。
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInfoInterceptor()).addPathPatterns("/**");
    }
}
