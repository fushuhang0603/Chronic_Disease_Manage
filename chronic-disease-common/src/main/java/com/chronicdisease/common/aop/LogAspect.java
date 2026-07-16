package com.chronicdisease.common.aop;

import com.alibaba.fastjson2.JSON;
import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.util.UserInfoContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Aspect
public class LogAspect {

    private static final int MAX_PARAM_LENGTH = 2000;
    private static final String TRACE_ID_KEY = "traceId";
    private static final String TRACE_HEADER = "X-Trace-Id";

    /** 切点：所有标注 @OperationLog 注解的方法 */
    @Pointcut("@annotation(com.chronicdisease.common.annotation.OperationLog)")
    public void logPointCut() {
    }

    //@annotation(operationLog)注解实例注入
    @Around("logPointCut() && @annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // traceId：优先取网关传入的 X-Trace-Id，否则生成一个
        String traceId = request != null ? request.getHeader(TRACE_HEADER) : null;
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        MDC.put(TRACE_ID_KEY, traceId);

        Long userId = UserInfoContext.getUserId();
        String method = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : "UNKNOWN";
        String params = serializeArgs(joinPoint.getArgs());

        Object result;
        String status = "成功";
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            status = "失败 - " + e.getMessage();
            throw e;
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            String ip = getClientIp(request);
            log.info("[操作日志] traceId:{} | {}-{} | {} {} | 用户:{} | 参数:{} | IP:{} | 耗时:{}ms | {}",
                    traceId,
                    operationLog.module(), operationLog.description(),
                    method, uri,
                    userId, params, ip, elapsed, status);
            MDC.remove(TRACE_ID_KEY);
        }
        return result;
    }

    /**
     * 安全序列化方法参数
     * - MultipartFile → 记录文件名和大小
     * - HttpServletRequest/Response → 跳过
     * - InputStream/OutputStream → 跳过
     * - 其他 → JSON序列化，超长截断
     */
    private String serializeArgs(Object[] args) {
        List<String> parts = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null) continue;
            if (arg instanceof MultipartFile file) {
                parts.add(String.format("[文件:%s, %.1fKB]", file.getOriginalFilename(), file.getSize() / 1024.0));
            } else if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                continue;
            } else if (arg instanceof InputStream || arg instanceof OutputStream) {
                continue;
            } else {
                try {
                    String json = JSON.toJSONString(arg);
                    if (json.length() > MAX_PARAM_LENGTH) {
                        json = json.substring(0, MAX_PARAM_LENGTH) + "...(截断)";
                    }
                    parts.add(json);
                } catch (Exception e) {
                    parts.add("[无法序列化]");
                }
            }
        }
        return parts.isEmpty() ? "无" : String.join(", ", parts);
    }

    /** 获取客户端真实IP，优先取 X-Forwarded-For */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
