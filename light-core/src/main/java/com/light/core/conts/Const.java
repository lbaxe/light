package com.light.core.conts;

public class Const {
    public static final String DATA = "data";

    public static final String TRACE_ID = "traceId";

    public static final String CURRENT_USER_ID = "currentUserId";
    /**
     * 请求起始url
     */
    public static final String INITIAL_URL = "initialUrl";
    /**
     * 当前请求的url
     */
    public static final String CURRENT_URL = "currentUrl";
    /**
     * 最近请求客户端ip
     */
    public static final String CLIENT_IP = "clientIP";
    /**
     * RPC调用URL命名空间
     */
    public static final String NAMESPACE_API = "RPC";
    /**
     * 自定义日志目录logger命名空间
     */
    public static final String NAMESPACE_LOG4J_CATEGORY = "light.log4j.category.";

    public static class CLIENT_HEADER {
        public static final String X_CLIENT_TRACE_ID = "X-Client-Trace-Id";
        public static final String X_CLIENT_IP = "X-Client-IP";
    }
}
