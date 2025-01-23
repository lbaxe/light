package com.light.web.common.oauth.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.light.framework.cache.redis.IRedisClient;
import com.light.web.common.oauth.OAuthUtil;
import com.light.web.common.oauth.mapper.OAuth2AuthorizedClientMapper;
import com.light.web.common.oauth.mapper.entity.OAuth2AuthorizedClient;
import com.light.web.common.oauth.mapper.entity.OAuth2Client;
import com.light.web.common.oauth.service.AuthzServerService;
import com.light.web.common.oauth.service.OAuth2ClientService;

@Service
public class AuthzServerServiceImpl implements AuthzServerService {
    @Autowired
    private OAuth2AuthorizedClientMapper auth2AuthorizedClientMapper;
    @Autowired
    private IRedisClient redisClient;
    @Autowired
    private OAuth2ClientService oAuth2ClientService;

    @Override
    public void validAuthzRequest(OAuthAuthzRequest oauthRequest, OAuth2Client oAuth2Client)
        throws OAuthProblemException {
        // https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2
        if (oAuth2Client == null) {
            throw OAuthProblemException.error(OAuthError.CodeResponse.UNAUTHORIZED_CLIENT)
                .description(OAuthUtil.INVALID_CLIENT_DESCRIPTION)
                // .uri("")
                .responseStatus(HttpServletResponse.SC_FOUND).state(oauthRequest.getState());
        }
        // 验证客户端允许的授权类型
        Set<String> authorizedGrantTypes =
            Stream.of(oAuth2Client.getAuthorizedGrantTypes().split(",")).map(String::trim).collect(Collectors.toSet());
        if (!authorizedGrantTypes.contains(GrantType.AUTHORIZATION_CODE.toString())) {
            throw OAuthProblemException.error(OAuthError.CodeResponse.UNAUTHORIZED_CLIENT)
                .description(OAuthUtil.INVALID_CLIENT_DESCRIPTION)
                // .uri("")
                .responseStatus(HttpServletResponse.SC_FOUND).state(oauthRequest.getState());
        }
        // 验证客户端允许的授权范围
        Set<String> scopes = oauthRequest.getScopes();
        Set<String> clientScopses =
            Stream.of(oAuth2Client.getScope().split(",")).map(String::trim).collect(Collectors.toSet());
        scopes.removeAll(clientScopses);
        if (!scopes.isEmpty()) {
            throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_SCOPE).description("invalid scope")
                // .uri("")
                .responseStatus(HttpServletResponse.SC_FOUND).state(oauthRequest.getState());
        }
    }

    @Override
    public void validTokenRequest(OAuthTokenRequest oauthRequest, OAuth2Client oAuth2Client,
        boolean isHeaderAuthorization) throws OAuthProblemException {
        GrantType grantType = OAuthUtil.convertGrantType(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE));
        switch (grantType) {
            case AUTHORIZATION_CODE:
                validTokenRequest4AuthorizationCode(oauthRequest, oAuth2Client, isHeaderAuthorization);
                // 失效授权码
                redisClient.del(oAuth2Client.getClientId());
                break;
            case CLIENT_CREDENTIALS:
                validTokenRequest4ClientCredentials(oauthRequest, oAuth2Client, true, isHeaderAuthorization);
                break;
            case REFRESH_TOKEN:
                validTokenRequest4RefreshToken(oauthRequest, oAuth2Client, isHeaderAuthorization);
                break;
            case PASSWORD:
                validTokenRequest4Password(oauthRequest, oAuth2Client, isHeaderAuthorization);
                break;
        }
    }

    private void validTokenRequest4AuthorizationCode(OAuthTokenRequest oauthRequest, OAuth2Client oAuth2Client,
        boolean isHeaderAuthorization) throws OAuthProblemException {
        this.validTokenRequest4ClientCredentials(oauthRequest, oAuth2Client, true, isHeaderAuthorization);

        Map<String, String> authorizedData = redisClient.hGetAll(oAuth2Client.getClientId());
        // 验证授权码
        String codeCache = authorizedData.get(OAuth.OAUTH_CODE);
        if (codeCache == null || !codeCache.equals(oauthRequest.getCode())) {
            throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_GRANT)
                .description("invalid authorization code").responseStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        // 验证重定向链接
        String redirectUri = authorizedData.get(OAuth.OAUTH_REDIRECT_URI);
        if (redirectUri == null || !redirectUri.equals(oauthRequest.getRedirectURI())) {
            throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_GRANT)
                .description("invalid redirect uri").responseStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        // 验证scope是否与授权申请相同
        String scopeCache = authorizedData.get(OAuth.OAUTH_SCOPE);
        if (scopeCache == null) {
            throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_SCOPE).description("invalid scope")
                .responseStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        Set<String> scopeCacheSet = Stream.of(scopeCache).map(String::trim).collect(Collectors.toSet());
        boolean isSameSize = scopeCacheSet.size() == oauthRequest.getScopes().size();
        // 取差集
        Set<String> requestScopes = new HashSet<>(oauthRequest.getScopes());
        requestScopes.removeAll(scopeCacheSet);
        if (!isSameSize || !requestScopes.isEmpty()) {
            throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_SCOPE).description("invalid scope")
                .responseStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void validTokenRequest4Password(OAuthTokenRequest oauthRequest, OAuth2Client oAuth2Client,
        boolean isHeaderAuthorization) throws OAuthProblemException {
        this.validTokenRequest4ClientCredentials(oauthRequest, oAuth2Client, true, isHeaderAuthorization);
        // TODO 验证用户名密码
    }

    private void validTokenRequest4RefreshToken(OAuthTokenRequest oauthRequest, OAuth2Client oAuth2Client,
        boolean isHeaderAuthorization) throws OAuthProblemException {
        this.validTokenRequest4ClientCredentials(oauthRequest, oAuth2Client, true, isHeaderAuthorization);

        OAuth2AuthorizedClient oAuth2AuthorizedClient =
            oAuth2ClientService.getLatestOAuth2AuthorizedClientByClientId(oauthRequest.getClientId());
        if (oAuth2AuthorizedClient == null) {
            throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_GRANT)
                .description("invalid refresh token").responseStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        // 保证上次的refresh_token失效
        if (!oAuth2AuthorizedClient.getRefreshTokenValue().equals(oauthRequest.getRefreshToken())) {
            throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_GRANT)
                .description("invalid refresh token").responseStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        // 验证refresh_token有效期
        if (!oAuth2AuthorizedClient.getRefreshTokenExpiresAt().after(new Date())) {
            throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_GRANT).description("invalid refresh")
                .responseStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        // 验证scope
        Set<String> preScopes =
            Stream.of(oAuth2AuthorizedClient.getAccessTokenScopes()).map(String::trim).collect(Collectors.toSet());
        Set<String> requestScopes = new HashSet<>();
        // 不可以超出上一次申请的范围，如果省略该参数，则表示与上一次一致。
        if (oauthRequest.getScopes().isEmpty()) {
            requestScopes.addAll(preScopes);
        } else {
            requestScopes.addAll(oauthRequest.getScopes());
        }
        requestScopes.removeAll(preScopes);
        // 超过上次scope范围
        if (!requestScopes.isEmpty()) {
            throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_SCOPE).description("invalid scope")
                .responseStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void validTokenRequest4ClientCredentials(OAuthTokenRequest oauthRequest, OAuth2Client oAuth2Client,
        boolean isValidSecret, boolean isHeaderAuthorization) throws OAuthProblemException {
        // https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2
        if (oAuth2Client == null) {
            if (isHeaderAuthorization) {
                throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_CLIENT)
                    .description(OAuthUtil.INVALID_CLIENT_DESCRIPTION)
                    .responseStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_CLIENT)
                .description(OAuthUtil.INVALID_CLIENT_DESCRIPTION).responseStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        // 验证客户端允许的授权类型
        Set<String> authorizedGrantTypes =
            Stream.of(oAuth2Client.getAuthorizedGrantTypes().split(",")).map(String::trim).collect(Collectors.toSet());
        if (!authorizedGrantTypes.contains(GrantType.AUTHORIZATION_CODE.toString())) {
            throw OAuthProblemException.error(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT)
                .description(OAuthUtil.INVALID_CLIENT_DESCRIPTION).responseStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        // check if client_secret is valid
        if (isValidSecret && !oAuth2Client.getClientSecret().equals(oauthRequest.getClientSecret())) {
            throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_GRANT)
                .description(OAuthUtil.INVALID_CLIENT_DESCRIPTION).responseStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        Set<String> scopes = oauthRequest.getScopes();
        Set<String> clientScopses =
            Stream.of(oAuth2Client.getScope().split(",")).map(String::trim).collect(Collectors.toSet());
        scopes.removeAll(clientScopses);
        if (!scopes.isEmpty()) {
            throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_SCOPE).description("invalid scope")
                .responseStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
