package com.light.framework.mvc.config;

import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.light.framework.mvc.controller.ErrorController;
import com.light.framework.mvc.filter.light.LightFilter;
import com.light.framework.mvc.http.XssCommonsMultipartResolver;

// @EnableWebMvc 开启注解会关闭WebMvcAutoConfiguration自动配置
@Configuration
@EnableConfigurationProperties({ServerProperties.class})
public class ExtendWebMvcConfiguration implements WebMvcConfigurer, ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    public FilterRegistrationBean<Filter> lightFilerRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean();
        registration.setFilter(new LightFilter());
        registration.addUrlPatterns("/*");
        registration.setName("lightFiler");
        registration.setDispatcherTypes(
            EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR));
        // 优先级在RequestContextFilter之后
        registration.setOrder(OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER - 1);
        return registration;
    }


    @Bean
    public XssCommonsMultipartResolver multipartResolver() {
        XssCommonsMultipartResolver multipartResolver = new XssCommonsMultipartResolver();
        multipartResolver.setDefaultEncoding("utf-8");
        return multipartResolver;
    }

    @Bean
    public DefaultErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes();
    }
    @Bean
    public ErrorController basicErrorController(ErrorAttributes errorAttributes, ServerProperties serverProperties,
        ObjectProvider<List<ErrorViewResolver>> errorViewResolversProvider) {
        ErrorProperties errorProperties = serverProperties.getError();
        errorProperties.setIncludeException(true);
        errorProperties.setIncludeStacktrace(ErrorProperties.IncludeAttribute.ALWAYS);
        errorProperties.setIncludeBindingErrors(ErrorProperties.IncludeAttribute.ALWAYS);
        errorProperties.setIncludeMessage(ErrorProperties.IncludeAttribute.ALWAYS);
        return new ErrorController(errorAttributes, errorProperties, errorViewResolversProvider.getIfAvailable());
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add((request, response, handler, ex) -> {
            System.out.println("hhhhhhhhhhhhhhhhhhhhhhh");
            return null;
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws Exception {
                return HandlerInterceptor.super.preHandle(request, response, handler);
            }

            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                ModelAndView modelAndView) throws Exception {
                HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
            }

            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                Exception ex) throws Exception {
                HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
            }
        });
    }
}
