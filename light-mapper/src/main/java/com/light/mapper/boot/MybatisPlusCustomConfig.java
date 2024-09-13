package com.light.mapper.boot;

import com.light.mapper.injector.ExtendSqlInjector;
import com.light.mapper.magic.ExtendMybatisConfiguration;
import com.light.mapper.plugin.DataEncryptDecryptPlugin;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

@Configuration
@AutoConfigureBefore(MybatisPlusCustomAutoConfiguration.class)
public class MybatisPlusCustomConfig {
    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public ConfigurationCustomizer configurationCustomizer(RedisTemplate<String, Object> redisTemplate) {
        return configuration -> {
            if (configuration instanceof ExtendMybatisConfiguration) {
                ExtendMybatisConfiguration extendMybatisConfiguration = (ExtendMybatisConfiguration)configuration;
                extendMybatisConfiguration.setRedisTemplate(redisTemplate);
            }
        };
    }

    @Bean
    public DataEncryptDecryptPlugin dataEncryptDecryptPlugin() {
        return new DataEncryptDecryptPlugin();
    }

    /**
     * 自定义sql注入器
     */
    @Bean
    public ISqlInjector iSqlInjector() {
        return new ExtendSqlInjector();
    }

    /**
     * 分页
     *
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
