package com.light.framework.cache.redis.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.light.common.text.TextUtil;
import com.light.framework.cache.redis.IRedisClient;
import com.light.framework.cache.redis.RedisClientContext;

@Configuration
public class RedisCustomConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(RedisCustomConfiguration.class);

    @Bean
    @ConditionalOnSingleCandidate(RedisConnectionFactory.class)
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        RedisConnection redisConnection = connectionFactory.getConnection();
        logger.info("Redis connectioned to {}", redisConnection.info("server").getProperty("tcp_port"));

        return template;
    }

    @Bean
    @ConditionalOnSingleCandidate(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> redisTemplate1(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate();
        template.setConnectionFactory(connectionFactory);
        RedisConnection redisConnection = connectionFactory.getConnection();
        logger.info("Redis connectioned to {}", redisConnection.info("server").getProperty("tcp_port"));

        return template;
    }

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public IRedisClient redisClient(StringRedisTemplate stringRedisTemplate) {
        return (IRedisClient)Proxy.newProxyInstance(RedisClientContext.class.getClassLoader(),
            RedisClientContext.class.getInterfaces(),
            new RedisClientInterceptor(new RedisClientContext(stringRedisTemplate)));
    }

    private class RedisClientInterceptor implements InvocationHandler {
        private final Logger logger = LoggerFactory.getLogger(RedisClientInterceptor.class);
        private Object targetObj;

        public RedisClientInterceptor(Object targetObj) {
            this.targetObj = targetObj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = null;
            long st = System.currentTimeMillis();
            try {
                result = method.invoke(targetObj, args);
            } catch (Throwable t) {
                Throwable actual = t;
                if (actual instanceof InvocationTargetException) {
                    actual = ((InvocationTargetException)actual).getTargetException();
                }
                logger.error("redis调用异常，method={}", method.getName(), actual);
            } finally {
                logger.info("redis -> method={},args={},time={}ms", method.getName(),
                    TextUtil.desensitize(Arrays.toString(args)), System.currentTimeMillis() - st);
            }
            return result;
        }
    }

}