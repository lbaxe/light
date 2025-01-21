
package com.light.web.common.oauth;

import java.io.Serializable;

import org.apache.oltu.oauth2.common.token.BasicOAuthToken;
import org.apache.shiro.authc.AuthenticationToken;
import org.springframework.util.Assert;

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

    /**
     * Access Token Types.
     *
     * @see <a target="_blank" href= "https://tools.ietf.org/html/rfc6749#section-7.1">Section 7.1 Access Token
     *      Types</a>
     */
    public static final class TokenType implements Serializable {
        private static final long serialVersionUID = 9035558805083173876L;

        public static final TokenType BEARER = new TokenType("Bearer");

        private final String value;

        private TokenType(String value) {
            Assert.hasText(value, "value cannot be empty");
            this.value = value;
        }

        /**
         * Returns the value of the token type.
         * 
         * @return the value of the token type
         */
        public String getValue() {
            return this.value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            TokenType that = (TokenType)obj;
            return this.getValue().equalsIgnoreCase(that.getValue());
        }

        @Override
        public int hashCode() {
            return this.getValue().hashCode();
        }

    }

}
