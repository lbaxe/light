package com.light.core.util;

public class TraceIdUtil {
    public static TraceId createTraceId() {
        TraceId traceId = new TraceId();
        ThreadLocalUtil.getInstance().setTraceId(traceId);
        return traceId;
    }

    public static String nextTraceId() {
        TraceId currentTraceId = ThreadLocalUtil.getInstance().<TraceId>getTraceId();
        if (currentTraceId == null) {
            currentTraceId = createTraceId();
        }
        currentTraceId.iterate();
        return currentTraceId.toString();
    }

    public static String getTraceId() {
        TraceId currentTraceId = ThreadLocalUtil.getInstance().<TraceId>getTraceId();
        if (currentTraceId == null)
            currentTraceId = createTraceId();
        return currentTraceId.toString();
    }

    public static TraceId trace(String traceId) {
        if (traceId == null) {
            return createTraceId();
        }
        TraceId currentTraceId = new TraceId(traceId);
        ThreadLocalUtil.getInstance().setTraceId(currentTraceId);
        return currentTraceId;
    }
}
