package com.light.framework.mvc.security.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.light.core.conts.Const;
import com.light.framework.mvc.response.AjaxResult;
import com.light.framework.mvc.security.*;
import com.light.framework.mvc.security.filter.JwtAuthenticationFilter;
import com.light.framework.mvc.security.filter.MultiLoginTypeAuthenticationFilter;
import com.light.framework.mvc.security.provider.DelegatingUserDetailsAuthenticationProvider;

@Configuration // (proxyBeanMethods = false)
@EnableWebSecurity
@ConditionalOnProperty(prefix = "security", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConfigurationProperties(prefix = "security.ignore")
public class LightWebSecurityConfiguration {
    @Autowired
    ObjectProvider<PermissionService> permissionServiceObjectProvider;

    public String[] getWhites() {
        return whites;
    }

    public void setWhites(String[] whites) {
        this.whites = whites;
    }

    private String[] whites;

    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            web.ignoring()
                // 后缀匹配静态资源
                .regexMatchers("/(.*)\\.(shtml|html|css|js|gif|png|jpeg|jpg|ico|txt|xml)$")
                // 目录匹配静态资源
                .antMatchers("/css/**", "/js/**", "/images/**", "/v3/api-docs/**", "/swagger-resources/**",
                    "/webjars/**")
                // error
                .antMatchers("/error/**")
                // 预检请求
                .antMatchers(HttpMethod.OPTIONS, "/**");
            if (whites != null && whites.length > 0) {
                // 非登录访问白名单
                web.ignoring().antMatchers(whites);
            }
        };
    }

    @Bean
    AuthenticationManager authenticationManager() {
        return new ProviderManager(delegatingUserDetailsAuthenticationProvider());
    }

    @Bean
    DelegatingUserDetailsAuthenticationProvider delegatingUserDetailsAuthenticationProvider() {
        return new DelegatingUserDetailsAuthenticationProvider();
    }

    @Bean
    SecurityFilterChain jwtToken2SecurityFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic().disable();
        // 关闭csrf攻击拦截
        http.csrf().disable();
        // 关闭默认表单登录，不支持前后端分离
        http.formLogin().disable();
        // 禁用登出页
        http.logout().disable();
        // 禁用session，jwt无状态不依赖session/cookie
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        RequestMatcher rpcMatcher = new AntPathRequestMatcher("/" + Const.NAMESPACE_API + "/**");
        RequestMatcher loginMatcher = new AntPathRequestMatcher("/login**");
        RequestMatcher notRpcMatcher = new NegatedRequestMatcher(rpcMatcher);
        RequestMatcher notLoginMatcher = new NegatedRequestMatcher(loginMatcher);
        RequestMatcher andMatcher = new AndRequestMatcher(Arrays.asList(notRpcMatcher, notLoginMatcher));
        // 拦截所有非RPC请求
        http.requestMatcher(andMatcher).authorizeRequests().anyRequest().authenticated();
        // 处理带token请求
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter();
        jwtAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        // jwt
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.authenticationProvider(delegatingUserDetailsAuthenticationProvider());
        return http.build();
    }

    /**
     * 账密登录
     * 
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    SecurityFilterChain usernamepaswordLogin2SecurityFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic().disable();
        // 关闭csrf攻击拦截
        http.csrf().disable();
        // 关闭默认表单登录，不支持前后端分离
        http.formLogin().disable();
        // 禁用登出页
        http.logout().disable();
        // 禁用session，jwt无状态不依赖session/cookie
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // 认证异常
        http.exceptionHandling().authenticationEntryPoint(ajaxAuthenticationEntryPoint())
            .accessDeniedHandler(ajaxAccessDeniedHandler());

        http.authenticationProvider(delegatingUserDetailsAuthenticationProvider());
        // http.authenticationManager(authenticationManager());

        RequestMatcher loginMatcher = new AntPathRequestMatcher("/login");

        MultiLoginTypeAuthenticationFilter multiLoginTypeAuthenticationFilter =
            new MultiLoginTypeAuthenticationFilter(authenticationManager());
        multiLoginTypeAuthenticationFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            response.setStatus(401);
            response.getWriter().println(AjaxResult.error("401", exception.getMessage()));
        });

        http.requestMatcher(loginMatcher).authorizeRequests()
            .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                @Override
                public <O extends FilterSecurityInterceptor> O postProcess(O object) {
                    object.setSecurityMetadataSource(filterInvocationSecurityMetadataSource());
                    object.setAccessDecisionManager(new FullMatchAccessDecisionManager());
                    return object;
                }
            }).anyRequest().authenticated().and()
            .addFilterBefore(multiLoginTypeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 手机验证码登录
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    SecurityFilterChain mobilephoneLogin2SecurityFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic().disable();
        // 关闭csrf攻击拦截
        http.csrf().disable();
        // 关闭默认表单登录，不支持前后端分离
        http.formLogin().disable();
        // 禁用登出页
        http.logout().disable();
        // 禁用session，jwt无状态不依赖session/cookie
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        RequestMatcher loginMatcher = new AntPathRequestMatcher("/login?t=p");

        MultiLoginTypeAuthenticationFilter multiLoginTypeAuthenticationFilter =
            new MultiLoginTypeAuthenticationFilter(EnumLoginType.MOBILEPHONE, loginMatcher);

        http.requestMatcher(loginMatcher).authorizeRequests().anyRequest().authenticated().and()
            .addFilterBefore(multiLoginTypeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.authenticationProvider(delegatingUserDetailsAuthenticationProvider());

        return http.build();
    }

    /**
     * 邮箱验证码登录
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    SecurityFilterChain mailLogin2SecurityFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic().disable();
        // 关闭csrf攻击拦截
        http.csrf().disable();
        // 关闭默认表单登录，不支持前后端分离
        http.formLogin().disable();
        // 禁用登出页
        http.logout().disable();
        // 禁用session，jwt无状态不依赖session/cookie
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        RequestMatcher loginMatcher = new AntPathRequestMatcher("/login?t=m");

        MultiLoginTypeAuthenticationFilter multiLoginTypeAuthenticationFilter =
            new MultiLoginTypeAuthenticationFilter(EnumLoginType.MAIL, loginMatcher);

        http.requestMatcher(loginMatcher).authorizeRequests().anyRequest().authenticated().and()
            .addFilterBefore(multiLoginTypeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.authenticationProvider(delegatingUserDetailsAuthenticationProvider());
        return http.build();
    }

    /**
     * oauth2授权登录
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    @ConditionalOnBean(OAuth2AuthorizedClientRepository.class)
    SecurityFilterChain outh2Login2SecurityFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic().disable();
        // 关闭csrf攻击拦截
        http.csrf().disable();
        // 关闭默认表单登录，不支持前后端分离
        http.formLogin().disable();
        // 禁用登出页
        http.logout().disable();
        // oauth2登录
        http.antMatcher("/oauth2/**").oauth2Login().loginProcessingUrl("/oauth2/code/*");
        return http.build();
    }

    /**
     * feign调用使用oauth2授权
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    SecurityFilterChain feignOauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic().disable();
        // 关闭csrf攻击拦截
        http.csrf().disable();
        // 关闭默认表单登录，不支持前后端分离
        http.formLogin().disable();
        // 禁用登出页
        http.logout().disable();

        RequestMatcher requestMatcher = new AntPathRequestMatcher("/" + Const.NAMESPACE_API + "/**");
        http.requestMatcher(requestMatcher).authorizeRequests().anyRequest().authenticated();

        http.authenticationProvider(delegatingUserDetailsAuthenticationProvider());
        return http.build();
    }

    @Bean
    AjaxAuthenticationEntryPoint ajaxAuthenticationEntryPoint() {
        return new AjaxAuthenticationEntryPoint();
    }

    @Bean
    AjaxAccessDeniedHandler ajaxAccessDeniedHandler() {
        return new AjaxAccessDeniedHandler();
    }

    @Bean
    InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        return new InMemoryUserDetailsManager();
    }

    @Bean
    RoleFilterInvocationSecurityMetadataSource filterInvocationSecurityMetadataSource() {
        return new RoleFilterInvocationSecurityMetadataSource(permissionServiceObjectProvider);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        String idForEncode = "bcrypt";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        return new DelegatingPasswordEncoder(idForEncode, encoders);
    }

    public enum EnumLoginType {
        USERNAMEPASSWORD, MOBILEPHONE, MAIL
    }
}
