package com.light.framework.mvc.config;

import java.util.*;

import javax.servlet.Filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.light.framework.mvc.filter.shiro.ExtendShiroFilterFactoryBean;
import com.light.framework.mvc.filter.shiro.ShiroFilterRegistrationBeanPostProcessor;
import com.light.framework.mvc.filter.shiro.external.ExternalShiroFilter;
import com.light.framework.mvc.filter.shiro.external.ExternalShiroGlobalFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * shiro 的基本配置 有不需要拦截的地址，需要在这里配置
 *
 * @author Administrator
 */
@Configuration
@Import({ShiroFilterRegistrationBeanPostProcessor.class})
@ConfigurationProperties(prefix = "security.ignore")
@Slf4j
public class ShiroConfiguration implements ApplicationContextAware {

    private List<String> whites;

    public List<String> getWhites() {
        return whites;
    }

    public void setWhites(List<String> whites) {
        this.whites = whites;
    }

    private List<Realm> realms;
    private ApplicationContext applicationContext;

    public ShiroConfiguration(ObjectProvider<List<Realm>> realmProvider) {
        this.realms = realmProvider.getObject();
    }

    /**
     * 开启注解方式控制访问url
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        return defaultAdvisorAutoProxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor =
            new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager());
        return authorizationAttributeSourceAdvisor;
    }

    @Bean
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealms(realms);
        // 默认使用ModularRealmAuthenticator 校验器 ，无需显示配置，默认处理策略AtLeastOneSuccessfulStrategy
        // ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
        // authenticator.setRealms(realms);
        // FirstSuccessfulStrategy firstSuccessfulStrategy = new FirstSuccessfulStrategy();
        // firstSuccessfulStrategy.setStopAfterFirstSuccess(true);
        // authenticator.setAuthenticationStrategy(firstSuccessfulStrategy);
        // securityManager.setAuthenticator(authenticator);

        // 关闭suject的session存储
        DefaultSubjectDAO defaultSubjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        defaultSubjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(defaultSubjectDAO);
        return securityManager;
    }

    @Bean
    @ConditionalOnBean(DefaultWebSecurityManager.class)
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        // ExternalShiroFilterPlugin externalFilterPlugin =
        // PluginContext.get().loadPlugin(ExternalShiroFilterPlugin.class);
        Map<String, ExternalShiroFilter> externalShiroFilterMap =
            applicationContext.getBeansOfType(ExternalShiroFilter.class);
        // 注意filter顺序
        Map<String, String> filterMap = new LinkedHashMap<>();

        // whites,白名单url，无需登录，除了anon不被其他任何filter拦截
        List<String> whites = this.getWhites();
        if (!CollectionUtils.isEmpty(whites)) {
            whites.forEach(e -> {
                filterMap.put(e, "anon");
            });
        }
        // 非白名单url，根据实际filter是否需要做拦截
        Map<String, Filter> externalFilterMap = new HashMap<>();
        // List<ExternalShiroFilter> externalShiroFilters = externalShiroFilterMap.getObject();
        externalShiroFilterMap.entrySet().forEach(e -> {
            ExternalShiroFilter filter = e.getValue();
            String filterChain = filterMap.get(filter.urlPattern());
            if (StringUtils.isBlank(filterChain)) {
                filterMap.put(filter.urlPattern(), filter.getClass().getSimpleName());
            } else if (!filterChain.contains("anon")) {
                filterMap.put(filter.urlPattern(), filterChain + "," + filter.getClass().getSimpleName());
            }

            externalFilterMap.put(filter.getClass().getSimpleName(), filter);
        });
        Map<String, ExternalShiroGlobalFilter> externalShiroGlobalFilterMap =
            applicationContext.getBeansOfType(ExternalShiroGlobalFilter.class);
        // 全局filter
        // Map<String, Filter> globalFiltersMap = new HashMap<>();
        // globalFiltersMap.put(UserContextFilter.class.getSimpleName(), new UserContextFilter());
        // globalFiltersMap.put(OriginalIpFilter.class.getSimpleName(), new OriginalIpFilter());
        // globalFiltersMap.put(XssFilter.class.getSimpleName(), new XssFilter());
        // globalFiltersMap.put(InputParamFilter.class.getSimpleName(), new InputParamFilter());

        Map<String, Filter> allFilters = new HashMap<>();
        allFilters.putAll(externalFilterMap);
        allFilters.putAll(externalShiroGlobalFilterMap);

        ExtendShiroFilterFactoryBean shiroFilterFactoryBean = new ExtendShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setFilters(allFilters);
        shiroFilterFactoryBean.setGlobalFilters(new ArrayList<>(externalShiroGlobalFilterMap.keySet()));
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterMap);

        return shiroFilterFactoryBean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
