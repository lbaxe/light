package com.light.framework.cache.local;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;

import net.sf.ehcache.Ehcache;

@Component
public class CacheService {
    @Autowired(required = false)
    private EhCacheCacheManager ehCacheCacheManager;

    private Cache getCache(String cacheName) {
        Cache cache = ehCacheCacheManager.getCache(cacheName);
        if (cache != null) {
            return cache;
        }
        // addCacheIfAbsent synchronized修饰方法,保证多线程环境下只add一次
        Ehcache ehcache = ehCacheCacheManager.getCacheManager().addCacheIfAbsent(cacheName);
        if (ehcache == null) {
            throw new RuntimeException("添加缓存失败");
        }
        return ehCacheCacheManager.getCache(cacheName);
    }

    public Object get(String cacheName, Object key) {
        Cache cache = this.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(key);
            if (valueWrapper != null) {
                return valueWrapper.get();
            }
        }
        return null;
    }

    public <T> T get(String cacheName, Object key, Class<T> clazz) {
        Cache cache = this.getCache(cacheName);
        if (cache != null) {
            return cache.get(key, clazz);
        }
        return null;
    }

    public <T> T get(String cacheName, Object key, Callable<T> callable) {
        Cache cache = this.getCache(cacheName);
        if (cache != null) {
            return cache.get(key, callable);
        }
        return null;
    }

    public void put(String cacheName, Object key, Object value) {
        Cache cache = this.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    public Object putIfAbsent(String cacheName, Object key, Object value) {
        Cache cache = this.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.putIfAbsent(key, value);
            if (valueWrapper != null) {
                return valueWrapper.get();
            }
        }
        return null;
    }

    public void evict(String cacheName, Object key) {
        Cache cache = this.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    public boolean evictIfPresent(String cacheName, Object key) {
        Cache cache = this.getCache(cacheName);
        if (cache != null) {
            return cache.evictIfPresent(key);
        }
        return false;
    }

    /**
     * 缓存有数据时返回true，否则false
     * 
     * @param cacheName
     * @return
     */
    public boolean invalidate(String cacheName) {
        Cache cache = this.getCache(cacheName);
        if (cache != null) {
            return cache.invalidate();
        }
        return false;
    }

    public boolean clear(String cacheName) {
        Cache cache = this.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
        return true;
    }
}