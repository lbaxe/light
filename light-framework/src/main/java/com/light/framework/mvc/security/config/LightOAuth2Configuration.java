package com.light.framework.mvc.security.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

// @Configuration(proxyBeanMethods = false)
// @EnableConfigurationProperties(OAuth2ClientProperties.class)
// @Conditional(MyOauth2Condition.class)
public class LightOAuth2Configuration {
    /**
     * A repository for OAuth 2.0
     * 
     * @param properties
     * @return
     */
    @Bean
    ClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {
        List<ClientRegistration> registrations =
            new ArrayList<>(OAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(properties).values());
        return new InMemoryClientRegistrationRepository(registrations);
    }

    // @Bean
    OAuth2AuthorizedClientService
        oAuth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    OAuth2AuthorizedClientService authorizedClientService(JdbcTemplate jdbcTemplate,
        ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);
    }

    @Bean
    OAuth2AuthorizedClientRepository authorizedClientRepository(OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }

    /**
     * AuthorizedClientServiceOAuth2AuthorizedClientManager 默认使用客户端模式
     * 
     * @param clientRegistrationRepository
     * @param oAuth2AuthorizedClientService
     * @return
     */
    @Bean
    OAuth2AuthorizedClientManager feignOAuth2AuthorizedClientManager(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
        return new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
            oAuth2AuthorizedClientService);
    }

}
