package com.light.web.common.oauth;

import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;

public final class OAuthUtil {
    public static final String INVALID_CLIENT_DESCRIPTION =
        "Client authentication failed (e.g., unknown client, no client authentication included, or unsupported authentication method).";

    public static GrantType convertGrantType(String grantType) {
        if (GrantType.AUTHORIZATION_CODE.toString().equals(grantType)) {
            return GrantType.AUTHORIZATION_CODE;
        } else if (GrantType.CLIENT_CREDENTIALS.toString().equals(grantType)) {
            return GrantType.CLIENT_CREDENTIALS;
        } else if (GrantType.PASSWORD.toString().equals(grantType)) {
            return GrantType.PASSWORD;
        } else if (GrantType.IMPLICIT.toString().equals(grantType)) {
            return GrantType.IMPLICIT;
        } else if (GrantType.REFRESH_TOKEN.toString().equals(grantType)) {
            return GrantType.REFRESH_TOKEN;
        } else if (GrantType.JWT_BEARER.toString().equals(grantType)) {
            return GrantType.JWT_BEARER;
        }
        return null;
    }

    public static ResponseType convertResponseType(String responseType) {
        if (ResponseType.CODE.toString().equals(responseType)) {
            return ResponseType.CODE;
        } else if (ResponseType.TOKEN.toString().equals(responseType)) {
            return ResponseType.TOKEN;
        }
        return null;
    }
}
