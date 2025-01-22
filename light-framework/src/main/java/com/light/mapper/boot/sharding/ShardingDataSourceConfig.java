//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.light.mapper.boot.sharding;

import java.sql.SQLException;
import java.util.*;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.shardingsphere.core.yaml.swapper.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShadowRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.api.EncryptDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.api.MasterSlaveDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.api.ShadowDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.spring.boot.common.SpringBootPropertiesConfigurationProperties;
import org.apache.shardingsphere.shardingjdbc.spring.boot.encrypt.EncryptRuleCondition;
import org.apache.shardingsphere.shardingjdbc.spring.boot.encrypt.SpringBootEncryptRuleConfigurationProperties;
import org.apache.shardingsphere.shardingjdbc.spring.boot.masterslave.MasterSlaveRuleCondition;
import org.apache.shardingsphere.shardingjdbc.spring.boot.masterslave.SpringBootMasterSlaveRuleConfigurationProperties;
import org.apache.shardingsphere.shardingjdbc.spring.boot.shadow.ShadowRuleCondition;
import org.apache.shardingsphere.shardingjdbc.spring.boot.shadow.SpringBootShadowRuleConfigurationProperties;
import org.apache.shardingsphere.shardingjdbc.spring.boot.sharding.ShardingRuleCondition;
import org.apache.shardingsphere.shardingjdbc.spring.boot.sharding.SpringBootShardingRuleConfigurationProperties;
import org.apache.shardingsphere.spring.boot.datasource.DataSourcePropertiesSetterHolder;
import org.apache.shardingsphere.spring.boot.util.DataSourceUtil;
import org.apache.shardingsphere.spring.boot.util.PropertyUtil;
import org.apache.shardingsphere.transaction.spring.ShardingTransactionTypeScanner;
import org.apache.shardingsphere.underlying.common.config.inline.InlineExpressionParser;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.jndi.JndiObjectFactoryBean;

import com.google.common.base.Preconditions;

import lombok.Generated;

@Configuration
@ComponentScan({"org.apache.shardingsphere.spring.boot.converter"})
@EnableConfigurationProperties({SpringBootShardingRuleConfigurationProperties.class,
    SpringBootMasterSlaveRuleConfigurationProperties.class, SpringBootEncryptRuleConfigurationProperties.class,
    SpringBootPropertiesConfigurationProperties.class, SpringBootShadowRuleConfigurationProperties.class})
@ConditionalOnProperty(prefix = "spring.shardingsphere", name = {"enabled"}, havingValue = "true")
public class ShardingDataSourceConfig implements EnvironmentAware {
    private final SpringBootShardingRuleConfigurationProperties shardingRule;
    private final SpringBootMasterSlaveRuleConfigurationProperties masterSlaveRule;
    private final SpringBootEncryptRuleConfigurationProperties encryptRule;
    private final SpringBootShadowRuleConfigurationProperties shadowRule;
    private final SpringBootPropertiesConfigurationProperties props;
    private final Map<String, DataSource> dataSourceMap = new LinkedHashMap();
    private final String jndiName = "jndi-name";

    @Bean
    @Conditional({ShardingRuleCondition.class})
    public DataSource shardingDataSource() throws SQLException {
        return ShardingDataSourceFactory.createDataSource(this.dataSourceMap,
            (new ShardingRuleConfigurationYamlSwapper()).swap(this.shardingRule), this.props.getProps());
    }

    @Bean
    @Conditional({MasterSlaveRuleCondition.class})
    public DataSource masterSlaveDataSource() throws SQLException {
        return MasterSlaveDataSourceFactory.createDataSource(this.dataSourceMap,
            (new MasterSlaveRuleConfigurationYamlSwapper()).swap(this.masterSlaveRule), this.props.getProps());
    }

    @Bean
    @Conditional({EncryptRuleCondition.class})
    public DataSource encryptDataSource() throws SQLException {
        return EncryptDataSourceFactory.createDataSource((DataSource)this.dataSourceMap.values().iterator().next(),
            (new EncryptRuleConfigurationYamlSwapper()).swap(this.encryptRule), this.props.getProps());
    }

    @Bean
    @Conditional({ShadowRuleCondition.class})
    public DataSource shadowDataSource() throws SQLException {
        return ShadowDataSourceFactory.createDataSource(this.dataSourceMap,
            (new ShadowRuleConfigurationYamlSwapper()).swap(this.shadowRule), this.props.getProps());
    }

    @Bean
    public ShardingTransactionTypeScanner shardingTransactionTypeScanner() {
        return new ShardingTransactionTypeScanner();
    }

    @Override
    public final void setEnvironment(Environment environment) {
        String prefix = "spring.shardingsphere.datasource.";
        Iterator var3 = this.getDataSourceNames(environment, prefix).iterator();

        while (var3.hasNext()) {
            String each = (String)var3.next();

            try {
                this.dataSourceMap.put(each, this.getDataSource(environment, prefix, each));
            } catch (ReflectiveOperationException var6) {
                throw new ShardingSphereException("Can't find datasource type!", var6);
            } catch (NamingException var7) {
                throw new ShardingSphereException("Can't find JNDI datasource!", var7);
            }
        }

    }

    private List<String> getDataSourceNames(Environment environment, String prefix) {
        StandardEnvironment standardEnv = (StandardEnvironment)environment;
        standardEnv.setIgnoreUnresolvableNestedPlaceholders(true);
        return null == standardEnv.getProperty(prefix + "name")
            ? (new InlineExpressionParser(standardEnv.getProperty(prefix + "names"))).splitAndEvaluate()
            : Collections.singletonList(standardEnv.getProperty(prefix + "name"));
    }

    private DataSource getDataSource(Environment environment, String prefix, String dataSourceName)
        throws ReflectiveOperationException, NamingException {
        Map<String, Object> dataSourceProps =
            (Map)PropertyUtil.handle(environment, prefix + dataSourceName.trim(), Map.class);
        Preconditions.checkState(!dataSourceProps.isEmpty(), "Wrong datasource properties!");
        if (dataSourceProps.containsKey("jndi-name")) {
            return this.getJndiDataSource(dataSourceProps.get("jndi-name").toString());
        } else {
            DataSource result = DataSourceUtil.getDataSource(dataSourceProps.get("type").toString(), dataSourceProps);
            DataSourcePropertiesSetterHolder.getDataSourcePropertiesSetterByType(dataSourceProps.get("type").toString())
                .ifPresent((dataSourcePropertiesSetter) -> {
                    dataSourcePropertiesSetter.propertiesSet(environment, prefix, dataSourceName, result);
                });
            return result;
        }
    }

    private DataSource getJndiDataSource(String jndiName) throws NamingException {
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setResourceRef(true);
        bean.setJndiName(jndiName);
        bean.setProxyInterface(DataSource.class);
        bean.afterPropertiesSet();
        return (DataSource)bean.getObject();
    }

    @Generated
    public ShardingDataSourceConfig(SpringBootShardingRuleConfigurationProperties shardingRule,
        SpringBootMasterSlaveRuleConfigurationProperties masterSlaveRule,
        SpringBootEncryptRuleConfigurationProperties encryptRule,
        SpringBootShadowRuleConfigurationProperties shadowRule, SpringBootPropertiesConfigurationProperties props) {
        this.shardingRule = shardingRule;
        this.masterSlaveRule = masterSlaveRule;
        this.encryptRule = encryptRule;
        this.shadowRule = shadowRule;
        this.props = props;
    }
}
