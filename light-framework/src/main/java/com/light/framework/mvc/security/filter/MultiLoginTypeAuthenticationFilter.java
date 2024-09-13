/*
 * Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.light.framework.mvc.security.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import com.light.framework.mvc.security.config.LightWebSecurityConfiguration;
import com.light.framework.mvc.security.token.LoginAuthenticationToken;

public class MultiLoginTypeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String SPRING_SECURITY_FORM_PRINCIPAL_KEY = "username";

    public static final String SPRING_SECURITY_FORM_CREDENTIALS_KEY = "password";

    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER =
        new AntPathRequestMatcher("/login", "POST");

    private String principalParameter = SPRING_SECURITY_FORM_PRINCIPAL_KEY;

    private String credentialsParameter = SPRING_SECURITY_FORM_CREDENTIALS_KEY;

    private LightWebSecurityConfiguration.EnumLoginType enumLoginType =
        LightWebSecurityConfiguration.EnumLoginType.USERNAMEPASSWORD;

    private boolean postOnly = true;

    public MultiLoginTypeAuthenticationFilter() {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
    }

    public MultiLoginTypeAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
    }

    public MultiLoginTypeAuthenticationFilter(LightWebSecurityConfiguration.EnumLoginType enumLoginType,
        RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
        this.enumLoginType = enumLoginType;
    }

    public MultiLoginTypeAuthenticationFilter(LightWebSecurityConfiguration.EnumLoginType enumLoginType,
        RequestMatcher requiresAuthenticationRequestMatcher, AuthenticationManager authenticationManager) {
        super(requiresAuthenticationRequestMatcher, authenticationManager);
        this.enumLoginType = enumLoginType;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        String principal = obtainPrincipal(request);
        principal = (principal != null) ? principal.trim() : "";
        String credentials = obtainCredentials(request);
        credentials = (credentials != null) ? credentials : "";
        LoginAuthenticationToken authRequest =
            LoginAuthenticationToken.unauthenticated(enumLoginType, principal, credentials);
        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Nullable
    protected String obtainCredentials(HttpServletRequest request) {
        return request.getParameter(this.credentialsParameter);
    }

    @Nullable
    protected String obtainPrincipal(HttpServletRequest request) {
        return request.getParameter(this.principalParameter);
    }

    protected void setDetails(HttpServletRequest request, LoginAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    public void setPrincipalParameter(String principalParameter) {
        Assert.hasText(principalParameter, "principal parameter must not be empty or null");
        this.principalParameter = principalParameter;
    }

    public void setCredentialsParameter(String credentialsParameter) {
        Assert.hasText(credentialsParameter, "credentials parameter must not be empty or null");
        this.credentialsParameter = credentialsParameter;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public String getPrincipalParameter() {
        return principalParameter;
    }

    public String getCredentialsParameter() {
        return credentialsParameter;
    }
}
