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

package com.light.framework.mvc.security.token;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.util.Assert;

import com.light.framework.mvc.security.config.LightWebSecurityConfiguration;

public class LoginAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private final Object principal;

    private Object credentials;

    private LightWebSecurityConfiguration.EnumLoginType enumLoginType;

    /**
     * This constructor can be safely used by any code that wishes to create a
     * <code>UsernamePasswordAuthenticationToken</code>, as the {@link #isAuthenticated()} will return
     * <code>false</code>.
     *
     */
    public LoginAuthenticationToken(LightWebSecurityConfiguration.EnumLoginType enumLoginType, Object principal,
        Object credentials) {
        super(null);
        this.enumLoginType = enumLoginType;
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false);
    }

    /**
     * This constructor should only be used by <code>AuthenticationManager</code> or <code>AuthenticationProvider</code>
     * implementations that are satisfied with producing a trusted (i.e. {@link #isAuthenticated()} = <code>true</code>)
     * authentication token.
     * 
     * @param principal
     * @param credentials
     * @param authorities
     */
    public LoginAuthenticationToken(LightWebSecurityConfiguration.EnumLoginType enumLoginType, Object principal,
        Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.enumLoginType = enumLoginType;
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true); // must use super, as we override
    }

    /**
     * This factory method can be safely used by any code that wishes to create a unauthenticated
     * <code>UsernamePasswordAuthenticationToken</code>.
     * 
     * @param principal
     * @param credentials
     * @return UsernamePasswordAuthenticationToken with false isAuthenticated() result
     *
     * @since 5.7
     */
    public static LoginAuthenticationToken unauthenticated(LightWebSecurityConfiguration.EnumLoginType enumLoginType,
        Object principal, Object credentials) {
        return new LoginAuthenticationToken(enumLoginType, principal, credentials);
    }

    /**
     * This factory method can be safely used by any code that wishes to create a authenticated
     * <code>UsernamePasswordAuthenticationToken</code>.
     * 
     * @param principal
     * @param credentials
     * @return UsernamePasswordAuthenticationToken with true isAuthenticated() result
     *
     * @since 5.7
     */
    public static LoginAuthenticationToken authenticated(LightWebSecurityConfiguration.EnumLoginType enumLoginType,
        Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        return new LoginAuthenticationToken(enumLoginType, principal, credentials, authorities);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    public LightWebSecurityConfiguration.EnumLoginType getEnumLoginType() {
        return enumLoginType;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated,
            "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }

}
