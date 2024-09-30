package com.light.core.util;

import java.util.HashMap;
import java.util.Map;

import com.light.core.conts.Const;

public class ThreadLocalUtil extends ThreadLocal<Map<String, Object>> {
    @Override
    protected Map<String, Object> initialValue() {
        return new HashMap<>(15);
    }

    private static final ThreadLocalUtil instance = new ThreadLocalUtil();

    public static ThreadLocalUtil getInstance() {
        return instance;
    }

    @Override
    public void remove() {
        get().clear();
    }

    public Object get(String key) {
        return get().get(key);
    }

    public void set(String key, Object value) {
        get().put(key, value);
    }

    public Map<String, Object> getAll() {
        return get();
    }

    @Override
    public void set(Map<String, Object> map) {
        get().putAll(map);
    }

    public void setCurrentUserId(String currentUserId) {
        set(Const.CURRENT_USER_ID, currentUserId);
    }

    public String getCurrentUserId() {
        Object obj = get(Const.CURRENT_USER_ID);
        return (obj != null) ? (String)obj : "";
    }

    public void setClientIP(String clientIP) {
        set(Const.CLIENT_IP, clientIP);
    }

    public String getClientIP() {
        Object obj = get(Const.CLIENT_IP);
        return (obj != null) ? (String)obj : "";
    }

    public void setCurrentUrl(String currentUrl) {
        set(Const.CURRENT_URL, currentUrl);
    }

    public String getCurrentUrl() {
        Object obj = get(Const.CURRENT_URL);
        return (obj != null) ? (String)obj : "";
    }

    public <T> void setTraceId(T traceId) {
        set(Const.TRACE_ID, traceId);
    }

    public <T> T getTraceId() {
        Object obj = get(Const.TRACE_ID);
        return (obj != null) ? (T)obj : null;
    }
}
