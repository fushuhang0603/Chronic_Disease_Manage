package com.chronicdisease.common.util;

/**
 * 用户上下文（ThreadLocal），当前请求线程内共享
 */
public class UserInfoContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    /** 请求结束后清理，防止内存泄漏 */
    public static void clear() {
        USER_ID.remove();
    }
}
