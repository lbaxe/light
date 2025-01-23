package com.light.web.common.oauth.service;

import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;

import com.light.web.common.oauth.mapper.entity.OAuth2Client;

public interface AuthzServerService {
    /**
     * 校验授权请求
     * 
     * @param oauthRequest
     * @param oAuth2Client
     * @throws OAuthProblemException
     */
    void validAuthzRequest(OAuthAuthzRequest oauthRequest, OAuth2Client oAuth2Client) throws OAuthProblemException;

    /**
     * 校验授权请求
     *
     * @param oauthRequest
     * @param oAuth2Client
     * @param isHeaderAuthorization
     * @throws OAuthProblemException
     */
    void validTokenRequest(OAuthTokenRequest oauthRequest, OAuth2Client oAuth2Client, boolean isHeaderAuthorization)
        throws OAuthProblemException;
}
