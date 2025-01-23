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
import java.util.stream.Collectors;

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

import com.light.core.annotation.IgnoreResponseWrapper;
import com.light.core.exception.ServiceException;
import com.light.framework.cache.redis.IRedisClient;
import com.light.web.common.oauth.OAuthUtil;
import com.light.web.common.oauth.mapper.OAuth2AuthorizedClientMapper;
import com.light.web.common.oauth.mapper.entity.OAuth2AuthorizedClient;
import com.light.web.common.oauth.mapper.entity.OAuth2Client;
import com.light.web.common.oauth.service.AuthzServerService;
import com.light.web.common.oauth.service.OAuth2ClientService;

/**
 * Handles requests for the application welcome page.
 */
@Controller
@RequestMapping("/oauth")
public class AuthzServerController {
    @Autowired
    private OAuth2ClientService oAuth2ClientService;
    @Autowired
    private OAuth2AuthorizedClientMapper oAuth2AuthorizedClientMapper;
    @Autowired
    private AuthzServerService authzServerService;
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
    public void authorize(HttpServletRequest request, HttpServletResponse response)
        throws IOException, OAuthSystemException {
        OAuthAuthzRequest oauthRequest = null;
        OAuthResponse oauthResponse = null;
        String redirectURI = null;
        try {
            oauthRequest = new OAuthAuthzRequest(request);
            redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);
            // build response according to response_type
            ResponseType responseType = OAuthUtil.convertResponseType(oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE));
            // 暂不支持简化模式
            if (responseType == null || responseType == ResponseType.TOKEN) {
                throw OAuthProblemException.error(OAuthError.CodeResponse.UNSUPPORTED_RESPONSE_TYPE)
                    .description(OAuthUtil.INVALID_CLIENT_DESCRIPTION)
                    // .uri("")
                    .responseStatus(HttpServletResponse.SC_FOUND).state(oauthRequest.getState());
            }

            OAuth2Client oAuth2Client = oAuth2ClientService.getOAuth2Client(oauthRequest.getClientId());
            // 验证客户端和权限范围
            authzServerService.validAuthzRequest(oauthRequest, oAuth2Client);

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
                        throw OAuthProblemException.error(OAuthError.CodeResponse.SERVER_ERROR)
                            .description(OAuthUtil.INVALID_CLIENT_DESCRIPTION)
                            // .uri("")
                            .responseStatus(HttpServletResponse.SC_FOUND).state(oauthRequest.getState());
                    }
                    builder.setAccessToken(accessToken);
                    builder.setTokenType(TokenType.BEARER.toString());
                    builder.setExpiresIn(oAuth2Client.getAccessTokenValidity() + "");
                    break;
            }
            oauthResponse = builder.location(redirectURI).buildQueryMessage();
            // return new ModelAndView(new RedirectView(oauthResponse.getLocationUri()));
            response.sendRedirect(oauthResponse.getLocationUri());
        } catch (OAuthProblemException e) {
            if (redirectURI != null) {
                oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(e)
                    .location(redirectURI).buildQueryMessage();
                response.sendRedirect(oauthResponse.getLocationUri());
            } else {
                oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).error(e)
                    .buildJSONMessage();
                response.setStatus(oauthResponse.getResponseStatus());
                response.getWriter().write(oauthResponse.getBody());
            }
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

            GrantType grantType = OAuthUtil.convertGrantType(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE));
            if (grantType == null || grantType == GrantType.IMPLICIT || grantType == GrantType.JWT_BEARER) {
                throw OAuthProblemException.error(OAuthError.TokenResponse.UNSUPPORTED_GRANT_TYPE)
                    .description(OAuthUtil.INVALID_CLIENT_DESCRIPTION)
                    .responseStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            OAuth2Client oAuth2Client = oAuth2ClientService.getOAuth2Client(oauthRequest.getClientId());
            authzServerService.validTokenRequest(oauthRequest, oAuth2Client,
                request.getHeader(OAuth.HeaderType.AUTHORIZATION) == null);

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
                OAuthASResponse.errorResponse(e.getResponseStatus()).error(e).buildJSONMessage();
            response.setStatus(oauthResponse.getResponseStatus());
            response.getWriter().write(oauthResponse.getBody());
        }
    }
}
