package com.light.common.async;

import java.util.concurrent.ConcurrentHashMap;

public class AsyncService {
    private static final ConcurrentHashMap<Class<?>, Object> proxyCache = new ConcurrentHashMap<>();

    private static class AsyncServiceHolder {
        private static AsyncServiceProxyProcessor INSTANCE = new AsyncServiceProxyProcessor();
    }

    public static <T> T getProxy(T bean) {
        Class<?> clazz = bean.getClass();
        T proxy = (T)proxyCache.get(clazz);
        if (proxy == null) {
            proxyCache.put(clazz, proxy = AsyncServiceHolder.INSTANCE.getProxy(bean));
        }
        return proxy;
    }
}
