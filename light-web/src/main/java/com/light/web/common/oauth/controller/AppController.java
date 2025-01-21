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

import java.security.SecureRandom;

import javax.validation.constraints.NotBlank;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;

import com.light.core.exception.ServiceException;
import com.light.web.common.oauth.mapper.OAuth2ClientMapper;
import com.light.web.common.oauth.mapper.entity.OAuth2Client;
import com.light.web.common.oauth.service.OAuth2ClientService;

import cn.hutool.crypto.digest.DigestUtil;

@Validated
@Controller("/oauth/app")
public class AppController {
    @Autowired
    private OAuth2ClientService authzServerService;
    @Autowired
    private OAuth2ClientMapper oAuth2ClientMapper;

    private final SecureRandom random = new SecureRandom();

    /**
     * 注册OAuth应用
     * 
     * @param appName
     * @param appIcon
     * @param appUrl
     * @param appDescription
     * @param appRedirectUri
     * @param scope
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public void register(@NotBlank(message = "应用名称 必填") String appName, String appIcon, String appUrl,
        @NotBlank(message = "应用描述 必填") String appDescription, @NotBlank(message = "授权回调应用的接口 必填") String appRedirectUri,
        String scope) {
        MD5Generator generator = new MD5Generator();
        String clientId = null;
        try {
            clientId = generator.generateValue();
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }
        OAuth2Client oAuth2Client = authzServerService.getOAuth2Client(clientId);
        if (oAuth2Client != null) {
            throw new ServiceException("系统繁忙，稍后重试");
        }
        byte[] bytes = new byte[10];
        String clientSecret = DigestUtil.sha256Hex(bytes);
        OAuth2Client client = new OAuth2Client();
        client.setClientId(clientId);
        client.setClientSecret(clientSecret);
        client.setAppName(appName);
        client.setAppIcon(appIcon);
        client.setAppUrl(appUrl);
        client.setAppDescription(appDescription);
        client.setAppRedirectUri(appRedirectUri);
        client.setAuthorizedGrantTypes(GrantType.CLIENT_CREDENTIALS.toString());
        client.setAuthorities("");
        client.setScope(scope);
        client.setAccessTokenValidity(3600 * 2);
        client.setRefreshTokenValidity(3600 * 8);
        oAuth2ClientMapper.insert(client);
    }

    /**
     * 编辑OAuth应用信息
     * 
     * @param clientId
     * @param appName
     * @param appIcon
     * @param appUrl
     * @param appDescription
     * @param appRedirectUri
     * @param scope
     */
    @PostMapping(value = "/edit", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public void edit(@NotBlank(message = "参数异常") String clientId, @NotBlank(message = "应用名称 必填") String appName,
        String appIcon, String appUrl, @NotBlank(message = "应用描述 必填") String appDescription,
        @NotBlank(message = "授权回调应用的接口 必填") String appRedirectUri, String scope) {
        OAuth2Client oAuth2Client = authzServerService.getOAuth2Client(clientId);
        if (oAuth2Client != null) {
            throw new ServiceException("系统繁忙，稍后重试");
        }
        OAuth2Client client = new OAuth2Client();
        client.setId(oAuth2Client.getId());
        client.setAppName(appName);
        client.setAppIcon(appIcon);
        client.setAppUrl(appUrl);
        client.setAppDescription(appDescription);
        client.setAppRedirectUri(appRedirectUri);
        client.setScope(scope);
        oAuth2ClientMapper.updateById(client);
    }

    /**
     * 删除OAuth应用信息
     * 
     * @param clientId
     */
    @PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@NotBlank(message = "参数异常") String clientId) {
        OAuth2Client oAuth2Client = authzServerService.getOAuth2Client(clientId);
        if (oAuth2Client == null) {
            return;
        }
        oAuth2ClientMapper.deleteById(oAuth2Client.getClientId());
    }

    /**
     * 重置OAuth应用秘钥
     * 
     * @param clientId
     */
    @PostMapping(value = "/updateSecret", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateSecret(@NotBlank(message = "参数异常") String clientId) {
        OAuth2Client oAuth2Client = authzServerService.getOAuth2Client(clientId);
        if (oAuth2Client != null) {
            throw new ServiceException("系统繁忙，稍后重试");
        }
        byte[] bytes = new byte[10];
        String clientSecret = DigestUtil.sha256Hex(bytes);
        OAuth2Client client = new OAuth2Client();
        client.setId(oAuth2Client.getId());
        client.setClientSecret(clientSecret);
        oAuth2ClientMapper.updateById(client);
    }

    /**
     * 移除OAuth应用已授权的tokens
     * 
     * @param clientId
     */
    @PostMapping(value = "/removeAccessTokens", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public void removeAccessTokens(@NotBlank(message = "参数异常") String clientId) {
        OAuth2Client oAuth2Client = authzServerService.getOAuth2Client(clientId);
        if (oAuth2Client == null) {
            return;
        }
        authzServerService.removeAccessTokens(oAuth2Client.getClientId());
    }
}
