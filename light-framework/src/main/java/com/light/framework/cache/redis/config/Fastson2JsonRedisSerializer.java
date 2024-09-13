/*
 * Copyright 2011-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.light.framework.cache.redis.config;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.light.framework.cache.exception.CacheException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Jackson2JsonRedisSerializer
 * 
 * @param <T>
 */
public class Fastson2JsonRedisSerializer<T> implements RedisSerializer<T> {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final JavaType javaType;

    public Fastson2JsonRedisSerializer(Class<T> type) {
        this.javaType = getJavaType(type);
    }

    public Fastson2JsonRedisSerializer(JavaType javaType) {
        this.javaType = javaType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(@Nullable byte[] bytes) throws CacheException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return JSON.parseObject(new String(bytes, DEFAULT_CHARSET), (Type)javaType.getRawClass(),
                JSONReader.Feature.SupportAutoType);
        } catch (Exception ex) {
            throw new CacheException("Could not read JSON: " + ex.getMessage(), ex);
        }
    }

    @Override
    public byte[] serialize(@Nullable T t) throws CacheException {
        if (t == null) {
            return new byte[0];
        }
        try {
            return JSON.toJSONString(t, JSONWriter.Feature.WriteClassName).getBytes(DEFAULT_CHARSET);
        } catch (Exception ex) {
            throw new CacheException("Could not write JSON: " + ex.getMessage(), ex);
        }
    }

    /**
     * Returns the Jackson {@link JavaType} for the specific class.
     * <p>
     * Default implementation returns
     * {@link TypeFactory#constructType(Type)}, but this can be
     * overridden in subclasses, to allow for custom generic collection handling.
     * For instance:
     *
     * <pre class="code">
     * protected JavaType getJavaType(Class&lt;?&gt; clazz) {
     *     if (List.class.isAssignableFrom(clazz)) {
     *         return TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, MyBean.class);
     *     } else {
     *         return super.getJavaType(clazz);
     *     }
     * }
     * </pre>
     *
     * @param clazz the class to return the java type for
     * @return the java type
     */
    protected JavaType getJavaType(Class<T> clazz) {
        return TypeFactory.defaultInstance().constructType(clazz);
    }
}
