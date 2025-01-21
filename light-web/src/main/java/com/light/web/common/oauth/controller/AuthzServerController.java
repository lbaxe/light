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
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.light.core.annotation.IgnoreResponseWrapper;
import com.light.core.exception.ServiceException;
import com.light.framework.cache.redis.IRedisClient;
import com.light.web.common.oauth.OAuth2AccessToken;
import com.light.web.common.oauth.mapper.entity.OAuth2AuthorizedClient;
import com.light.web.common.oauth.mapper.entity.OAuth2Client;
import com.light.web.common.oauth.service.OAuth2ClientService;

/**
 * Handles requests for the application welcome page.
 */
@Controller("/oauth")
public class AuthzServerController {

    public static final String INVALID_CLIENT_DESCRIPTION =
        "Client authentication failed (e.g., unknown client, no client authentication included, or unsupported authentication method).";
    @Autowired
    private OAuth2ClientService oAuth2ClientService;
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
    public ModelAndView authorize(HttpServletRequest request) throws ServiceException, OAuthSystemException {

        OAuthAuthzRequest oauthRequest = null;

        OAuthIssuerImpl oAuthIssuer = new OAuthIssuerImpl(new MD5Generator());
        OAuthResponse response = null;
        try {
            oauthRequest = new OAuthAuthzRequest(request);
            // build response according to response_type
            String responseType = oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);

            OAuthASResponse.OAuthAuthorizationResponseBuilder builder =
                OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND);

            if (responseType.equals(ResponseType.CODE.toString())) {
                String code = oAuthIssuer.authorizationCode();
                builder.setCode(code);
                // code 进行存储
                redisClient.set("client_id:" + oauthRequest.getClientId(), code, 60 * 5, TimeUnit.SECONDS);
            }
            if (responseType.equals(ResponseType.TOKEN.toString())) {
                // builder.setAccessToken(oauthIssuerImpl.accessToken());
                // builder.setTokenType(OAuth.DEFAULT_TOKEN_TYPE.toString());
                // builder.setExpiresIn(3600L);
                throw new ServiceException("not supported response_type : " + responseType);
            }
            String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);
            response = builder.location(redirectURI).buildQueryMessage();
        } catch (OAuthProblemException e) {
            response = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(e).location(e.getRedirectUri())
                .buildQueryMessage();
        }
        return new ModelAndView(new RedirectView(response.getLocationUri()));
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

        OAuthTokenRequest oauthRequest = null;
        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
        try {
            oauthRequest = new OAuthTokenRequest(request);

            GrantType grantType = GrantType.valueOf(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE));
            if (grantType != GrantType.AUTHORIZATION_CODE && grantType != GrantType.CLIENT_CREDENTIALS) {
                OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                    .buildJSONMessage();
                response.setStatus(oauthResponse.getResponseStatus());
                response.getWriter().write(oauthResponse.getBody());
                return;
            }
            OAuth2Client oAuth2Client = oAuth2ClientService.getOAuth2Client(oauthRequest.getClientId());

            if (oAuth2Client == null) {
                OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                    .buildJSONMessage();
                response.setStatus(oauthResponse.getResponseStatus());
                response.getWriter().write(oauthResponse.getBody());
                return;
            }
            // check if clientid is valid
            if (!oAuth2Client.getClientId().equals(oauthRequest.getClientId())) {
                OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                    .buildJSONMessage();
                response.setStatus(oauthResponse.getResponseStatus());
                response.getWriter().write(oauthResponse.getBody());
                return;
            }

            // check if client_secret is valid
            if (!oAuth2Client.getClientSecret().equals(oauthRequest.getClientSecret())) {
                OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT)
                    .setErrorDescription(INVALID_CLIENT_DESCRIPTION).buildJSONMessage();
                response.setStatus(oauthResponse.getResponseStatus());
                response.getWriter().write(oauthResponse.getBody());
                return;
            }
            // do checking for different grant types
            if (grantType == GrantType.AUTHORIZATION_CODE) {
                String code = oauthRequest.getCode();
                String codeCache = redisClient.get("client_id:" + oauthRequest.getClientId());
                if (codeCache == null || !codeCache.equals(code)) {
                    OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT)
                        .setErrorDescription("invalid authorization code").buildJSONMessage();
                    response.setStatus(oauthResponse.getResponseStatus());
                    response.getWriter().write(oauthResponse.getBody());
                    return;
                }
            } else if (grantType == GrantType.PASSWORD) {
                /*if (!Common.PASSWORD.equals(oauthRequest.getPassword())
                    || !Common.USERNAME.equals(oauthRequest.getUsername())) {
                    OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT)
                        .setErrorDescription("invalid username or password").buildJSONMessage();
                    response.setStatus(oauthResponse.getResponseStatus());
                    response.getWriter().write(oauthResponse.getBody());
                    return;
                }*/
            } else if (grantType == GrantType.REFRESH_TOKEN) {
                List<OAuth2AuthorizedClient> auth2AuthorizedClientList =
                    oAuth2ClientService.getAllValidOAuth2AuthorizedClients(oauthRequest.getClientId(), null);
                OAuthTokenRequest finalOauthRequest = oauthRequest;
                boolean isValidRefreshToken = auth2AuthorizedClientList.stream()
                    .anyMatch(e -> e.getRefreshTokenIssuedAt().compareTo(new Date()) > 0
                        && e.getRefreshTokenValue().equals(finalOauthRequest.getRefreshToken()));
                if (auth2AuthorizedClientList.isEmpty() || !isValidRefreshToken) {
                    OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription("invalid refresh")
                        .buildJSONMessage();
                    response.setStatus(oauthResponse.getResponseStatus());
                    response.getWriter().write(oauthResponse.getBody());
                }
                return;
            }

            OAuthResponse oauthResponse =
                OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK).setAccessToken(oauthIssuerImpl.accessToken())
                    .setTokenType(OAuth2AccessToken.TokenType.BEARER.getValue())
                    .setExpiresIn(oAuth2Client.getAccessTokenValidity().toString())
                    .setRefreshToken(oauthIssuerImpl.refreshToken()).buildJSONMessage();

            response.setStatus(oauthResponse.getResponseStatus());
            response.getWriter().write(oauthResponse.getBody());

        } catch (OAuthProblemException e) {
            OAuthResponse oauthResponse =
                OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e).buildJSONMessage();
            response.setStatus(oauthResponse.getResponseStatus());
            response.getWriter().write(oauthResponse.getBody());
        }
    }
}
