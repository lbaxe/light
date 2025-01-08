package com.light.framework.mvc.config;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.*;

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

import com.light.framework.mvc.filter.shiro.ShiroFilterRegistrationBeanPostProcessor;
import com.light.framework.mvc.filter.shiro.ShiroGlobalFilter;
import com.light.framework.mvc.filter.shiro.ShiroProxyChainFilter;

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

    /**
     * Spring容器中的realms
     */
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

    // @Bean
    // public DefaultWebSessionManager sessionManager() {
    // DefaultWebSessionManager manager = new DefaultWebSessionManager();
    // // 加入缓存管理器
    // manager.setCacheManager(getEhCacheManager());
    // // 删除过期的session
    // manager.setDeleteInvalidSessions(true);
    // // 设置全局session超时时间
    // manager.setGlobalSessionTimeout(expireTime * 60 * 1000);
    // // 去掉 JSESSIONID
    // manager.setSessionIdUrlRewritingEnabled(false);
    // // 定义要使用的无效的Session定时调度器
    // manager.setSessionValidationScheduler();
    // // 是否定时检查session
    // manager.setSessionValidationSchedulerEnabled(true);
    // // 自定义SessionDao
    // manager.setSessionDAO(sessionDAO());
    // // 自定义sessionFactory
    // manager.setSessionFactory(sessionFactory());
    // return manager;
    // }

    @Bean
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // set realms
        securityManager.setRealms(realms);
        // 默认使用ModularRealmAuthenticator 校验器 ，无需显示配置，默认处理策略AtLeastOneSuccessfulStrategy
        // ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
        // authenticator.setRealms(realms);
        // FirstSuccessfulStrategy firstSuccessfulStrategy = new FirstSuccessfulStrategy();
        // firstSuccessfulStrategy.setStopAfterFirstSuccess(true);
        // authenticator.setAuthenticationStrategy(firstSuccessfulStrategy);
        // securityManager.setAuthenticator(authenticator);

        // 框架默认采用jwt实现身份认证，关闭session/cookie模式
        // 关闭记住我，不注入记住我管理器
        securityManager.setRememberMeManager(null);
        // 关闭session，不注入缓存管理器
        securityManager.setCacheManager(null);
        // 关闭session，不注入session管理器
        // securityManager.setSessionManager(null);
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        // 关闭session，不存储session
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        // SubjectDao是必须得，关闭session时，subject不保存到session且不做任何存储，使用SecurityUtils获取subject根据securityManager上下文重新创建
        DefaultSubjectDAO defaultSubjectDAO = new DefaultSubjectDAO();
        defaultSubjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(defaultSubjectDAO);
        return securityManager;
    }

    @Bean
    @ConditionalOnBean(DefaultWebSecurityManager.class)
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        Map<String, ShiroProxyChainFilter> shiroProxyChainFilterMap =
            applicationContext.getBeansOfType(ShiroProxyChainFilter.class);
        // 注意filter顺序
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();

        // whites,白名单url，无需登录，除了anon不被其他任何filter拦截
        List<String> whites = this.getWhites();
        if (!CollectionUtils.isEmpty(whites)) {
            whites.forEach(e -> {
                filterChainDefinitionMap.put(e, "anon");
            });
        }
        List<ShiroProxyChainFilter> shiroProxyChainFilters =
            shiroProxyChainFilterMap.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());
        shiroProxyChainFilters.sort((o1, o2) -> {
            if (o1.priority() == o2.priority()) {
                return 0;
            } else {
                return o1.priority() > o2.priority() ? -1 : 1;
            }
        });
        // 非白名单url，根据实际filter是否需要做拦截
        for (ShiroProxyChainFilter filter : shiroProxyChainFilters) {
            String preChainName = filterChainDefinitionMap.get(filter.urlPattern());
            String nextChainName = filter.getClass().getSimpleName();
            if (StringUtils.isBlank(preChainName)) {
                filterChainDefinitionMap.put(filter.urlPattern(), nextChainName);
            } else {
                // 不覆盖匿名配置
                if (preChainName.contains("anon")) {
                    continue;
                }
                filterChainDefinitionMap.put(filter.urlPattern(), preChainName + "," + nextChainName);
            }
        }
        Map<String, ShiroGlobalFilter> shiroGlobalFilterMap =
            applicationContext.getBeansOfType(ShiroGlobalFilter.class);

        Map<String, Filter> allFilters = new HashMap<>();
        allFilters.putAll(shiroProxyChainFilterMap);
        allFilters.putAll(shiroGlobalFilterMap);

        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setFilters(allFilters);
        shiroFilterFactoryBean.setGlobalFilters(new ArrayList<>(shiroGlobalFilterMap.keySet()));
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        return shiroFilterFactoryBean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static void main(String[] args) {
        List<ShiroProxyChainFilter> shiroProxyChainFilters = new ArrayList<>();
        ShiroProxyChainFilter filter = new ShiroProxyChainFilter() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                FilterChain filterChain) throws IOException, ServletException {

            }

            @Override
            public int priority() {
                return 1;
            }

            @Override
            public String urlPattern() {
                return "/*";
            }
        };
        ShiroProxyChainFilter filter1 = new ShiroProxyChainFilter() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                FilterChain filterChain) throws IOException, ServletException {

            }

            @Override
            public int priority() {
                return 2;
            }

            @Override
            public String urlPattern() {
                return "/*";
            }
        };
        shiroProxyChainFilters.add(filter);
        shiroProxyChainFilters.add(filter1);
        Collections.sort(shiroProxyChainFilters, (o1, o2) -> {
            if (o1.priority() == o2.priority()) {
                return 0;
            } else {
                return o1.priority() > o2.priority() ? -1 : 1;
            }
        });
        shiroProxyChainFilters.forEach(f -> {
            System.out.println(f.getClass().getCanonicalName() + " = " + f.priority());
        });
    }
}
