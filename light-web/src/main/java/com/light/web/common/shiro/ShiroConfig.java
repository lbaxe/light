package com.light.web.common.shiro;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.light.framework.mvc.filter.shiro.permission.FullMatchPermissionResolver;
import com.light.web.common.shiro.credentials.JwtTokenCredentialsMatcher;
import com.light.web.common.shiro.credentials.PwdCredentialsMatcher;
import com.light.web.common.shiro.realms.JwtTokenRealm;
import com.light.web.common.shiro.realms.UsernamePasswordRealm;

/**
 * shiro 的基本配置 有不需要拦截的地址，需要在这里配置
 *
 * @author Administrator
 */
@Configuration
public class ShiroConfig {

    @Bean
    public CredentialsMatcher loginCredentialsMatcher() {
        return new PwdCredentialsMatcher();
    }

    @Bean
    public CredentialsMatcher jwtTokenCredentialsMatcher() {
        return new JwtTokenCredentialsMatcher();
    }

    /**
     * 创建 JdbcAuthenticationRealm
     */
    @Bean
    public UsernamePasswordRealm createPwdAuthenticationRealm() {
        UsernamePasswordRealm realm = new UsernamePasswordRealm();
        realm.setCredentialsMatcher(loginCredentialsMatcher());
        realm.setPermissionResolver(new FullMatchPermissionResolver());
        return realm;
    }

    /**
     * 创建 TokenAuthenticationRealm
     */
    @Bean
    public JwtTokenRealm createJwtTokenAuthenticationRealm() {
        JwtTokenRealm realm = new JwtTokenRealm();
        realm.setCredentialsMatcher(jwtTokenCredentialsMatcher());
        realm.setPermissionResolver(new FullMatchPermissionResolver());
        return realm;
    }
}
