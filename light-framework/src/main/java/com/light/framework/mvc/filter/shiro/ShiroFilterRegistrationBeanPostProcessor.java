package com.light.framework.mvc.filter.shiro;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

public class ShiroFilterRegistrationBeanPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory)beanFactory;
        // 找到所有自定义的ShiroFilter，并注册FilterRegistrationBean
        String[] beanNames = beanFactory.getBeanNamesForType(ShiroFlagFilter.class);
        for (String beanName : beanNames) {
            registerFilterRegistrationBean(defaultListableBeanFactory, beanName);
        }
    }

    private static void registerFilterRegistrationBean(DefaultListableBeanFactory beanFactory, String beanName) {
        BeanDefinitionBuilder beanDefinitionBuilder =
            BeanDefinitionBuilder.genericBeanDefinition(FilterRegistrationBean.class);
        beanDefinitionBuilder.addPropertyReference("filter", beanName);
        beanDefinitionBuilder.addPropertyValue("enabled", false);
        beanFactory.registerBeanDefinition(beanName + "FilterRegistrationBean",
            beanDefinitionBuilder.getRawBeanDefinition());
    }
}