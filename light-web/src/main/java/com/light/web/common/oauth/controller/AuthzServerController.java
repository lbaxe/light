/**
 * Copyright 2010 Newcastle University
 *
 * http://research.ncl.ac.uk/smart/
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.light.web.common.oauth.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.common.message.types.TokenType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.light.core.annotation.IgnoreResponseWrapper;
import com.light.core.exception.ServiceException;
import com.light.framework.cache.redis.IRedisClient;
import com.light.web.common.oauth.mapper.OAuth2AuthorizedClientMapper;
import com.light.web.common.oauth.mapper.entity.OAuth2AuthorizedClient;
import com.light.web.common.oauth.mapper.entity.OAuth2Client;
import com.light.web.common.oauth.service.OAuth2ClientService;

/**
 * Handles requests for the application welcome page.
 */
@Controller
@RequestMapping("/oauth")
public class AuthzServerController {

    public static final String INVALID_CLIENT_DESCRIPTION =
        "Client authentication failed (e.g., unknown client, no client authentication included, or unsupported authentication method).";
    @Autowired
    private OAuth2ClientService oAuth2ClientService;
    @Autowired
    private OAuth2AuthorizedClientMapper oAuth2AuthorizedClientMapper;
    @Autowired
    private IRedisClient redisClient;

    /**
     * Oauth2授权
     * 
     * @param request
     * @return
     * @throws ServiceException
     * @throws OAuthSystemException
     */
    @GetMapping("/authorize")
    @IgnoreResponseWrapper
    public ModelAndView authorize(HttpServletRequest request) throws OAuthSystemException {
        OAuthAuthzRequest oauthRequest = null;
        OAuthResponse oauthResponse = null;
        try {
            oauthRequest = new OAuthAuthzRequest(request);
            String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);
            // build response according to response_type
            ResponseType responseType = this.convertResponseType(oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE));
            // 暂不支持简化模式
            if (responseType == null || responseType == ResponseType.TOKEN) {
                oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .location(redirectURI)
                    .setError(OAuthError.TokenResponse.UNSUPPORTED_GRANT_TYPE)
                    .setErrorDescription(INVALID_CLIENT_DESCRIPTION).buildQueryMessage();
                return new ModelAndView(new RedirectView(oauthResponse.getLocationUri()));
            }

            OAuth2Client oAuth2Client = oAuth2ClientService.getOAuth2Client(oauthRequest.getClientId());
            // 验证客户端和权限范围
            oauthResponse = this.validClientAndScopes(oauthRequest, oAuth2Client);
            if (oauthResponse != null) {
                oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .location(redirectURI)
                    .setError(OAuthError.TokenResponse.UNSUPPORTED_GRANT_TYPE)
                    .setErrorDescription(INVALID_CLIENT_DESCRIPTION).buildQueryMessage();
                return new ModelAndView(new RedirectView(oauthResponse.getLocationUri()));
            }
            OAuthASResponse.OAuthAuthorizationResponseBuilder builder =
                OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND);
            OAuthIssuerImpl oAuthIssuer = new OAuthIssuerImpl(new MD5Generator());
            switch (responseType) {
                case CODE:
                    String code = oAuthIssuer.authorizationCode();
                    builder.setCode(code);
                    // code 进行存储
                    redisClient.hSet(oauthRequest.getClientId(), OAuth.OAUTH_CODE, code);
                    // scope 如果与客户端申请的范围一致，此项省略，默认取注册权限范围。
                    String scopes = oauthRequest.getScopes().isEmpty() ? oAuth2Client.getScope()
                        : oauthRequest.getScopes().stream().map(String::valueOf).collect(Collectors.joining(","));
                    redisClient.hSet(oauthRequest.getClientId(), OAuth.OAUTH_SCOPE, scopes);
                    redisClient.expire(oauthRequest.getClientId(), 60 * 5);
                    break;
                case TOKEN:
                    String accessToken = oAuthIssuer.accessToken();
                    OAuth2AuthorizedClient oAuth2AuthorizedClient =
                        oAuth2ClientService.getOAuth2AuthorizedClientByAccessToken(accessToken);
                    if (oAuth2AuthorizedClient != null) {
                        oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .location(redirectURI).setError(OAuthError.TokenResponse.INVALID_GRANT)
                            .setErrorDescription(INVALID_CLIENT_DESCRIPTION).buildQueryMessage();
                        return new ModelAndView(new RedirectView(oauthResponse.getLocationUri()));
                    }
                    builder.setAccessToken(accessToken);
                    builder.setTokenType(TokenType.BEARER.toString());
                    builder.setExpiresIn(oAuth2Client.getAccessTokenValidity() + "");
                    break;
            }
            oauthResponse = builder.location(redirectURI).buildQueryMessage();
            return new ModelAndView(new RedirectView(oauthResponse.getLocationUri()));
        } catch (OAuthProblemException e) {
            oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(e)
                .location(e.getRedirectUri())
                .buildQueryMessage();
            return new ModelAndView(new RedirectView(oauthResponse.getLocationUri()));
        }
    }

    /**
     * Oauth2 获取token
     *
     * @param request
     * @param response
     * @throws OAuthSystemException
     * @throws IOException
     */
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @IgnoreResponseWrapper
    public void token(HttpServletRequest request, HttpServletResponse response)
        throws OAuthSystemException, IOException {
        try {
            OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);

            GrantType grantType = this.convertGrantType(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE));
            if (grantType == null || grantType == GrantType.IMPLICIT || grantType == GrantType.JWT_BEARER) {
                OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuthError.TokenResponse.UNSUPPORTED_GRANT_TYPE)
                    .setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                    .buildJSONMessage();
                response.setStatus(oauthResponse.getResponseStatus());
                response.getWriter().write(oauthResponse.getBody());
                return;
            }
            OAuth2Client oAuth2Client = oAuth2ClientService.getOAuth2Client(oauthRequest.getClientId());
            OAuthResponse oAuthResponse = null;
            switch (grantType) {
                case AUTHORIZATION_CODE:
                    oAuthResponse = validAuthorizationCode(oauthRequest, oAuth2Client);
                    break;
                case CLIENT_CREDENTIALS:
                    oAuthResponse = validClientCredentials(oauthRequest, oAuth2Client);
                    break;
                case REFRESH_TOKEN:
                    oAuthResponse = validRefreshToken(oauthRequest, oAuth2Client);
                    break;
                case PASSWORD:
                    oAuthResponse = validPassword(oauthRequest, oAuth2Client);
                    break;
            }
            if (oAuthResponse != null) {
                response.setStatus(oAuthResponse.getResponseStatus());
                response.getWriter().write(oAuthResponse.getBody());
                return;
            }

            OAuth2AuthorizedClient auth2AuthorizedClient = null;

            OAuth2AuthorizedClient preOAuth2AuthorizedClient =
                oAuth2ClientService.getLatestOAuth2AuthorizedClientByClientId(oAuth2Client.getClientId());
            // 防止token频繁调用，增加如下限制条件，全部满足时，返回历史最新授权
            // 1、存在有效历史授权
            // 2、access_token和refresh_token有效期未发生变更
            // 3、access_token剩余有效时间大于占比
            if (preOAuth2AuthorizedClient != null
                && preOAuth2AuthorizedClient.getAccessTokenExpiresAt().after(new Date())) {
                LocalDateTime issuedAt = preOAuth2AuthorizedClient.getAccessTokenIssuedAt().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime expiresAt = preOAuth2AuthorizedClient.getAccessTokenExpiresAt().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
                long seconds = ChronoUnit.SECONDS.between(issuedAt, expiresAt);
                if (oAuth2Client.getAccessTokenValidity().longValue() == seconds) {
                    long offSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), expiresAt);
                    double ratio = (double)offSeconds / (double)seconds;
                    if (ratio > 0.5) {
                        auth2AuthorizedClient = preOAuth2AuthorizedClient;
                    }
                }
            }
            if (auth2AuthorizedClient == null) {
                OAuthIssuer oAuthIssuer = new OAuthIssuerImpl(new MD5Generator());
                OAuth2AuthorizedClient entity = new OAuth2AuthorizedClient();
                entity.setClientId(oAuth2Client.getClientId());
                entity.setAccessTokenType(TokenType.BEARER.toString());
                entity.setAccessTokenValue(oAuthIssuer.accessToken());
                Date now = new Date();
                entity.setAccessTokenIssuedAt(now);
                entity.setAccessTokenExpiresAt(Date.from(LocalDateTime.now()
                    .plusSeconds(oAuth2Client.getAccessTokenValidity()).atZone(ZoneId.systemDefault()).toInstant()));
                // 如果与客户端申请的范围一致，此项省略时，默认注册权限范围。
                String scopes = oauthRequest.getScopes().isEmpty() ? oAuth2Client.getScope()
                    : oauthRequest.getScopes().stream().map(String::valueOf).collect(Collectors.joining(","));
                entity.setAccessTokenScopes(scopes);
                entity.setRefreshTokenValue(oAuthIssuer.refreshToken());
                entity.setRefreshTokenIssuedAt(now);
                entity.setRefreshTokenExpiresAt(Date.from(LocalDateTime.now()
                    .plusSeconds(oAuth2Client.getRefreshTokenValidity()).atZone(ZoneId.systemDefault()).toInstant()));
                oAuth2AuthorizedClientMapper.insert(entity);
                auth2AuthorizedClient = entity;
            }

            OAuthResponse oauthResponse =
                OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(auth2AuthorizedClient.getAccessTokenValue())
                    .setTokenType(auth2AuthorizedClient.getAccessTokenType())
                    .setExpiresIn(oAuth2Client.getAccessTokenValidity().toString())
                    .setRefreshToken(auth2AuthorizedClient.getRefreshTokenValue()).buildJSONMessage();

            response.setStatus(oauthResponse.getResponseStatus());
            response.getWriter().write(oauthResponse.getBody());
        } catch (OAuthProblemException e) {
            OAuthResponse oauthResponse =
                OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e).buildJSONMessage();
            response.setStatus(oauthResponse.getResponseStatus());
            response.getWriter().write(oauthResponse.getBody());
        }
    }

    private OAuthResponse validAuthorizationCode(OAuthTokenRequest oauthRequest, OAuth2Client oAuth2Client)
        throws OAuthSystemException {
        OAuthResponse oAuthResponse = validClientCredentials(oauthRequest, oAuth2Client);
        if (oAuthResponse != null) {
            return oAuthResponse;
        }
        // 验证授权码
        String codeCache = redisClient.hGet(oAuth2Client.getClientId(), OAuth.OAUTH_CODE);
        if (codeCache == null || !codeCache.equals(oauthRequest.getCode())) {
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription("invalid authorization code")
                .buildJSONMessage();
        }
        // 验证scope是否与授权申请相同
        String scopeCache = redisClient.hGet(oAuth2Client.getClientId(), OAuth.OAUTH_SCOPE);
        if (scopeCache == null) {
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_SCOPE).setErrorDescription("invalid scope")
                .buildJSONMessage();
        }
        Set<String> scopeCacheSet = Stream.of(scopeCache).map(String::trim).collect(Collectors.toSet());
        boolean isSameSize = scopeCacheSet.size() == oauthRequest.getScopes().size();
        // 取差集
        Set<String> requestScopes = new HashSet<>(oauthRequest.getScopes());
        requestScopes.removeAll(scopeCacheSet);
        if (!isSameSize || !requestScopes.isEmpty()) {
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_SCOPE).setErrorDescription("invalid scope")
                .buildJSONMessage();
        }
        // 失效授权码
        redisClient.del(oAuth2Client.getClientId());
        return null;
    }

    private OAuthResponse validClientCredentials(OAuthTokenRequest oauthRequest, OAuth2Client oAuth2Client)
        throws OAuthSystemException {
        OAuthResponse oAuthResponse = this.validClientAndScopes(oauthRequest, oAuth2Client);
        if (oAuthResponse != null) {
            return oAuthResponse;
        }
        // check if client_secret is valid
        if (!oAuth2Client.getClientSecret().equals(oauthRequest.getClientSecret())) {
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                .setError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT).setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                .buildJSONMessage();
        }
        return null;
    }

    private OAuthResponse validPassword(OAuthTokenRequest oauthRequest, OAuth2Client oAuth2Client)
        throws OAuthSystemException {
        OAuthResponse oAuthResponse = this.validClientAndScopes(oauthRequest, oAuth2Client);
        if (oAuthResponse != null) {
            return oAuthResponse;
        }
        return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
            .setError(OAuthError.TokenResponse.UNSUPPORTED_GRANT_TYPE).setErrorDescription(INVALID_CLIENT_DESCRIPTION)
            .buildJSONMessage();
    }

    private OAuthResponse validRefreshToken(OAuthTokenRequest oauthRequest, OAuth2Client oAuth2Client)
        throws OAuthSystemException {
        OAuthResponse oAuthResponse = this.validClientCredentials(oauthRequest, oAuth2Client);
        if (oAuthResponse != null) {
            return oAuthResponse;
        }
        OAuth2AuthorizedClient oAuth2AuthorizedClient =
            oAuth2ClientService.getLatestOAuth2AuthorizedClientByClientId(oauthRequest.getClientId());
        if (oAuth2AuthorizedClient == null) {
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription("invalid refresh")
                .buildJSONMessage();
        }
        // 上次的refresh_token失效
        if (!oAuth2AuthorizedClient.getRefreshTokenValue().equals(oauthRequest.getRefreshToken())) {
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription("invalid refresh")
                .buildJSONMessage();
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
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_SCOPE).setErrorDescription("invalid scope")
                .buildJSONMessage();
        }
        // 验证refresh_token有效期
        if (!oAuth2AuthorizedClient.getRefreshTokenExpiresAt().after(new Date())) {
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription("invalid refresh")
                .buildJSONMessage();
        }
        return null;
    }

    private OAuthResponse validClientAndScopes(OAuthTokenRequest oauthRequest, OAuth2Client oAuth2Client)
        throws OAuthSystemException {
        OAuthResponse oAuthResponse = this.validClient(oAuth2Client);
        if (oAuthResponse != null) {
            return oAuthResponse;
        }
        Set<String> scopes = oauthRequest.getScopes();
        Set<String> clientScopses =
            Stream.of(oAuth2Client.getScope().split(",")).map(String::trim).collect(Collectors.toSet());
        scopes.removeAll(clientScopses);
        if (!scopes.isEmpty()) {
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_SCOPE).setErrorDescription("invalid scope")
                .buildJSONMessage();
        }
        return null;
    }

    private OAuthResponse validClientAndScopes(OAuthAuthzRequest oauthRequest, OAuth2Client oAuth2Client)
        throws OAuthSystemException {
        OAuthResponse oAuthResponse = this.validClient(oAuth2Client);
        if (oAuthResponse != null) {
            return oAuthResponse;
        }
        Set<String> scopes = oauthRequest.getScopes();
        Set<String> clientScopses =
            Stream.of(oAuth2Client.getScope().split(",")).map(String::trim).collect(Collectors.toSet());
        scopes.removeAll(clientScopses);
        if (!scopes.isEmpty()) {
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_SCOPE).setErrorDescription("invalid scope")
                .buildQueryMessage();
        }

        return null;
    }

    private OAuthResponse validClient(OAuth2Client oAuth2Client) throws OAuthSystemException {
        if (oAuth2Client == null) {
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                .buildJSONMessage();
        }
        return null;
    }

    private GrantType convertGrantType(String grantType) {
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

    private ResponseType convertResponseType(String responseType) {
        if (ResponseType.CODE.toString().equals(responseType)) {
            return ResponseType.CODE;
        } else if (ResponseType.TOKEN.toString().equals(responseType)) {
            return ResponseType.TOKEN;
        }
        return null;
    }
}
