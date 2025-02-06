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

package com.light.web.common.oauth.domain;

import lombok.Data;

/**
 *
 *
 *
 */
@Data
public class OAuthParams {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String authzEndpoint;
    private String tokenEndpoint;
    private String authzCode;
    private String accessToken;
    private Long expiresIn;
    private String refreshToken;
    private String scope;
    private String state;
    private String resourceUrl;
    private String resource;
    private String application;
    private String requestType;
    private String requestMethod;
    private String idToken;
    private String header;
    private String claimsSet;
    private String jwt;
    private boolean idTokenValid;

    private String errorMessage;
}
