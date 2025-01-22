
package com.light.web.common.oauth;

import org.apache.oltu.oauth2.common.message.types.TokenType;
import org.apache.oltu.oauth2.common.token.BasicOAuthToken;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc6749#section-1.4">Section 1.4 Access Token</a>
 */
public class OAuth2AccessToken extends BasicOAuthToken implements AuthenticationToken {
    private String clientId;

    public OAuth2AccessToken() {}

    public OAuth2AccessToken(String clientId, String accessToken, TokenType tokenType, Long expiresIn,
        String refreshToken, String scope) {
        this(clientId, accessToken, tokenType.toString(), expiresIn, refreshToken, scope);
    }

    public OAuth2AccessToken(String clientId, String accessToken, String tokenType, Long expiresIn, String refreshToken,
        String scope) {
        super(accessToken, tokenType, expiresIn, refreshToken);
        this.clientId = clientId;
    }

    @Override
    public Object getPrincipal() {
        return this.clientId;
    }

    @Override
    public Object getCredentials() {
        return this.accessToken;
    }

    public String getClientId() {
        return clientId;
    }
}
