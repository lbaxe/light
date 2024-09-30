package com.light.mapper.boot;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.provider.AbstractDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;

/**
 * 使用{@link com.baomidou.dynamic.datasource.annotation.DS}注解，切换数据源
 */
@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class MybatisPlusDataSourceCustomConfig {

    /**
     * 分表数据源名称
     */
    private static final String SHARDING_DATASOURCE_NAME = "sharding";

    private DynamicDataSourceProperties dynamicDataSourceProperties;

    /**
     * shardingjdbc有四种数据源，需要根据业务注入不同的数据源 1. 未使用分片, 脱敏的名称(默认): shardingDataSource; 2. 主从数据源: masterSlaveDataSource; 3.
     * 脱敏数据源：encryptDataSource; 4. 影子数据源：shadowDataSource
     */
    private ObjectFactory<ShardingDataSource> shardingDataSource;

    MybatisPlusDataSourceCustomConfig(DynamicDataSourceProperties dynamicDataSourceProperties,
        ObjectFactory<ShardingDataSource> shardingDataSource) {
        this.dynamicDataSourceProperties = dynamicDataSourceProperties;
        this.shardingDataSource = shardingDataSource;
    }

    @Bean
    @ConditionalOnBean(ShardingDataSource.class)
    public DynamicDataSourceProvider dynamicDataSourceProvider() {
        return new AbstractDataSourceProvider() {
            @Override
            public Map<String, DataSource> loadDataSources() {
                Map<String, DataSource> dataSourceMap = new HashMap<>();
                // 将 shardingjdbc 管理的数据源也交给动态数据源管
                dataSourceMap.put(SHARDING_DATASOURCE_NAME, shardingDataSource.getObject());
                return dataSourceMap;
            }
        };
    }

    /**
     * 将动态数据源设置为首选的 当spring存在多个数据源时, 自动注入的是首选的对象 设置为主要的数据源之后，就可以支持shardingjdbc原生的配置方式了
     *
     * @return
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource();
        dataSource.setPrimary(dynamicDataSourceProperties.getPrimary());
        dataSource.setStrict(dynamicDataSourceProperties.getStrict());
        dataSource.setStrategy(dynamicDataSourceProperties.getStrategy());
        dataSource.setP6spy(dynamicDataSourceProperties.getP6spy());
        dataSource.setSeata(dynamicDataSourceProperties.getSeata());
        return dataSource;
    }

    @Bean
    @Primary
    JdbcTemplate jdbcTemplate(DynamicRoutingDataSource dynamicRoutingDataSource) {
        return new JdbcTemplate(dynamicRoutingDataSource);
    }
}