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
public class OAuthRegParams extends OAuthParams {

    private String registrationType;
    private String name = "OAuth V2.0 Demo Application";
    private String url = "http://localhost:8080";
    private String description = "Demo Application of the OAuth V2.0 Protocol";
    private String icon = "http://localhost:8080/demo.png";
    private String registrationEndpoint;
}
