package com.light.framework.mvc.security.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.cloud.openfeign.security.OAuth2AccessTokenInterceptor;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

import com.light.rpc.config.ServicesDiscoveryClient;
import com.light.rpc.feign.CommonRequestInterceptor;
import com.light.rpc.feign.CompositeRequestInterceptor;
import com.light.rpc.feign.LightSpringMvcContract;

import feign.RequestInterceptor;

@Configuration
@ConditionalOnClass({FeignClient.class})
@AutoConfigureBefore({CommonsClientAutoConfiguration.class})
public class LightFeignConfiguration {
    @Autowired(required = false)
    private FeignClientProperties feignClientProperties;
    @Autowired(required = false)
    private List<AnnotatedParameterProcessor> parameterProcessors = new ArrayList<>();

    @Bean
    @ConditionalOnClass(SpringMvcContract.class)
    public LightSpringMvcContract feignContract(ConversionService feignConversionService) {
        boolean decodeSlash = feignClientProperties == null || feignClientProperties.isDecodeSlash();
        return new LightSpringMvcContract(parameterProcessors, feignConversionService, decodeSlash);
    }

    @Bean
    ServicesDiscoveryClient servicesDiscoveryClient() {
        return new ServicesDiscoveryClient();
    }

    @Bean
    @Order(0)
    public CommonRequestInterceptor commonRequestInterceptor() {
        return new CommonRequestInterceptor();
    }

    @Bean
    @Order(10)
    @ConditionalOnSingleCandidate
    public OAuth2AccessTokenInterceptor
        oauth2AccessTokenInterceptor(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
        return new OAuth2AccessTokenInterceptor(oAuth2AuthorizedClientManager);
    }

    @Bean
    FeignBuilderCustomizer feignBuilderCustomizer(ServicesDiscoveryClient servicesDiscoveryClient,
        List<RequestInterceptor> requestInterceptors) {
        return builder -> {
            List<RequestInterceptor> requestInterceptorList = new ArrayList<>();
            requestInterceptorList.add(new CompositeRequestInterceptor(servicesDiscoveryClient, requestInterceptors));
            builder.requestInterceptors(Collections.unmodifiableList(requestInterceptorList));
        };
    }
}
