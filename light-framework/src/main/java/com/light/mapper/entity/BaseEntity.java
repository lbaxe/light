package com.light.mapper.entity;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.light.mapper.util.EntityUtil;

public abstract class BaseEntity implements IEntity {
    private static final Logger logger = LoggerFactory.getLogger(BaseEntity.class);

    private static final ConcurrentMap<Class<?>, Metadata> METADATA_CACHE = new ConcurrentHashMap<>();

    @Override
    public Metadata metadata() {
        return metadata(getClass());
    }

    public static Metadata metadata(Class<?> clazz) {
        Metadata metadata = null;
        if (METADATA_CACHE.containsKey(clazz)) {
            metadata = METADATA_CACHE.get(clazz);
        } else {
            synchronized (clazz) {
                metadata = EntityUtil.getMetadata(clazz);
                METADATA_CACHE.put(clazz, metadata);
            }
        }
        return metadata;
    }

    public Object pkValue() {
        Class<?> clazz = this.getClass();
        Field field = EntityUtil.getPKField(clazz);
        if (field != null) {
            try {
                return field.get(this);
            } catch (Exception e) {
                logger.warn(clazz + " get pk value error", e);
            }
        }
        return null;
    }
}
