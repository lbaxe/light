package com.light.mapper.boot;

import java.util.Properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.light.mapper.entity.BaseEntity;
import com.light.mapper.injector.ExtendSqlInjector;
import com.light.mapper.magic.ExtendMybatisConfiguration;
import com.light.mapper.plugin.DataEncryptDecryptPlugin;

@Configuration
@EnableConfigurationProperties(MybatisPlusProperties.class)
public class MybatisPlusConfig {
    /**
     * 在自动配置类构造bean之前初始化一些配置
     */
    public MybatisPlusConfig(MybatisPlusProperties properties) {
        // properties.setConfigLocation();
        properties.setMapperLocations(new String[] {"classpath*:mapper/**.xml"});
        properties.setTypeAliasesPackage("com.light.**.entity");
        properties.setTypeAliasesSuperType(BaseEntity.class);
        // .setTypeHandlersPackage();
        // properties.setCheckConfigLocation();
        // properties.setExecutorType();
        // properties.setDefaultScriptingLanguageDriver();
        Properties configurationProperties = new Properties();
        configurationProperties.put("mapUnderscoreToCamelCase", true);
        configurationProperties.put("autoMappingUnknownColumnBehavior", true);
        properties.setConfigurationProperties(configurationProperties);
        // properties.setConfiguration();
        // properties.setTypeEnumsPackage();
        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        GlobalConfig.DbConfig dbConfig = globalConfig.getDbConfig();
        dbConfig.setLogicDeleteField("deleteFlag");
        dbConfig.setLogicDeleteValue("1");
        dbConfig.setLogicNotDeleteValue("0");
        properties.setGlobalConfig(globalConfig);
    }

    @Bean
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
