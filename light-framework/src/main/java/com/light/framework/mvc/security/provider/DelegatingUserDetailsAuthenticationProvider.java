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

package com.light.framework.mvc.security.provider;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class DelegatingUserDetailsAuthenticationProvider implements AuthenticationProvider, ApplicationContextAware {

    protected final Log logger = LogFactory.getLog(getClass());

    private ApplicationContext applicationContext;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Map<String, AuthenticationProvider> authenticationProviderMap =
            applicationContext.getBeansOfType(AuthenticationProvider.class);
        for (AuthenticationProvider provider : authenticationProviderMap.values()) {
            if (provider.supports(authentication.getClass())) {
                return provider.authenticate(authentication);
            }
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
