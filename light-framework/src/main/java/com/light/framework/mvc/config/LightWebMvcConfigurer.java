package com.light.framework.mvc.config;

import java.util.ArrayList;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.light.framework.mvc.config.extend.RpcRequestMappingHandlerMapping;
import com.light.framework.mvc.config.extend.RpcServiceMethodProcessor;
import com.light.framework.mvc.controller.ErrorController;
import com.light.framework.mvc.filter.LightFilter;
import com.light.framework.mvc.http.XssCommonsMultipartResolver;

@Configuration
public class LightWebMvcConfigurer implements WebMvcConfigurer, ApplicationContextAware {
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
        registration.setOrder(OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER - 104);
        return registration;
    }


    @Bean
    public XssCommonsMultipartResolver multipartResolver() {
        XssCommonsMultipartResolver multipartResolver = new XssCommonsMultipartResolver();
        multipartResolver.setDefaultEncoding("utf-8");
        return multipartResolver;
    }

    @Bean
    @ConditionalOnBean
    public ErrorController basicErrorController(ErrorAttributes errorAttributes, ServerProperties serverProperties,
        ObjectProvider<List<ErrorViewResolver>> errorViewResolversProvider) {
        ErrorProperties errorProperties = serverProperties.getError();
        errorProperties.setIncludeException(true);
        errorProperties.setIncludeStacktrace(ErrorProperties.IncludeAttribute.ALWAYS);
        errorProperties.setIncludeBindingErrors(ErrorProperties.IncludeAttribute.ALWAYS);
        errorProperties.setIncludeMessage(ErrorProperties.IncludeAttribute.ALWAYS);
        return new ErrorController(errorAttributes, errorProperties, errorViewResolversProvider.getIfAvailable());
    }

    /**
     * 为feign client层生成RequestMappingHandlerMapping注册到springmvc容器
     *
     * @return
     */
    @Bean
    public WebMvcRegistrations webMvcRegistrations() {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new RpcRequestMappingHandlerMapping();
            }
        };
    }
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        // 配置jsp视图解析器
        // registry.jsp("/static/", ".jsp");
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(new HandlerExceptionResolver() {
            @Override
            public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                Object handler, Exception ex) {
                System.out.println("hhhhhhhhhhhhhhhhhhhhhhh");
                return null;
            }
        });
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // if (!registry.hasMappingForPattern("/static/**")) {
        // registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        // }
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        List<ControllerAdviceBean> adviceBeans = ControllerAdviceBean.findAnnotatedBeans(this.applicationContext);

        List<Object> requestResponseBodyAdviceBeans = new ArrayList<>();

        for (ControllerAdviceBean adviceBean : adviceBeans) {
            Class<?> beanType = adviceBean.getBeanType();
            if (beanType == null) {
                throw new IllegalStateException("Unresolvable type for ControllerAdviceBean: " + adviceBean);
            }
            if (RequestBodyAdvice.class.isAssignableFrom(beanType)
                || ResponseBodyAdvice.class.isAssignableFrom(beanType)) {
                requestResponseBodyAdviceBeans.add(adviceBean);
            }
        }
        List<HttpMessageConverter<?>> converters = messageConverters.getObject().getConverters();
        handlers.add(new RpcServiceMethodProcessor(converters, requestResponseBodyAdviceBeans));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
