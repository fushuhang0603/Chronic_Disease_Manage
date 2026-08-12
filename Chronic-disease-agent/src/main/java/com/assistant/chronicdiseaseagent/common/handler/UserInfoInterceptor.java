package com.assistant.chronicdiseaseagent.common.handler;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.assistant.chronicdiseaseagent.common.util.UserInfoContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取用户信息
        String userId = request.getHeader("user-info");
        String role = request.getHeader("user-role");
        //存入ThreadLocal
        if (StringUtils.isNotBlank(userId)) {
            UserInfoContext.setUserId(Long.valueOf(userId));
        }
        if (StringUtils.isNotBlank(role)) {
            UserInfoContext.setRole(role);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserInfoContext.clear();
    }
}
