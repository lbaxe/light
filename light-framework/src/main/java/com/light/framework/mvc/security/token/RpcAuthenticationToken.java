package com.light.framework.mvc.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class RpcAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal;

    private Object credentials;

    public RpcAuthenticationToken(Object principal, Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
