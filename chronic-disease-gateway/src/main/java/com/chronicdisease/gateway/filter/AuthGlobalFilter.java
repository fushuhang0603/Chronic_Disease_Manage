package com.chronicdisease.gateway.filter;

import com.chronicdisease.gateway.util.JwtTool;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;


@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 白名单路径,不需要验证token
     */
    private static final List<String> WHITELIST = List.of(
            "/user/login",
            "/user/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        //如果是白名单路径往下游传递
        if(isWhitePath(path)){
            return chain.filter(exchange);
        }

        //获取token
        String token = null;
        List<String> headers = exchange.getRequest().getHeaders().get("authorization");
        if(headers != null && !headers.isEmpty()){
            token = headers.getFirst();
        }
        Long userId = null;
        String role = null;
        try {
            Map<String, Object> map = JwtTool.parseToken(token);
            if (map != null) {
                userId = (Long) map.get("userId");
                role = (String) map.get("role");
            }
        } catch (Exception e) {
            //拦截设置响应状态码
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //TODO对身份校验做处理
        //往下游过滤器传递用户信息,将用户id存到请求头
        String userInfo = String.valueOf(userId);
        ServerWebExchange newExchange = exchange
                .mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();

        //放行
        return chain.filter(newExchange);
    }

    /**
     * 优先级,值越小越优先
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }


    /**
     * 判断是否为白名单路径
     * @param path
     * @return
     */
    private boolean isWhitePath(String path) {
        return WHITELIST.stream().anyMatch(path::startsWith);
    }
}