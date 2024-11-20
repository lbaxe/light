package com.light.framework.mvc.filter.shiro.token;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * @author chenlk
 */
public class JwtToken implements AuthenticationToken {

    private String token;

    private String uri;

    private String hostAddress;

    public JwtToken(String token, String uri, String hostAddress) {
        this.token = token;
        this.uri = uri;
        this.hostAddress = hostAddress;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    public String getUri() {
        return uri;
    }

    public String getHostAddress() {
        return hostAddress;
    }
}
