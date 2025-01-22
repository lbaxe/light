package com.light.web.common.oauth.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.light.web.common.oauth.mapper.entity.OAuth2AuthorizedClient;
import com.light.web.common.oauth.mapper.entity.OAuth2Client;

public interface OAuth2ClientService extends IService<OAuth2Client> {
    /**
     * 获取客户端注册信息
     * 
     * @param clientId
     * @return
     */
    OAuth2Client getOAuth2Client(String clientId);

    /**
     * 获取客户端最新的授权记录
     * 
     * @param clientId
     * @return
     */
    OAuth2AuthorizedClient getLatestOAuth2AuthorizedClientByClientId(String clientId);

    /**
     * 获取客户端的授权记录
     *
     * @param accessToken
     * @return
     */
    OAuth2AuthorizedClient getOAuth2AuthorizedClientByAccessToken(String accessToken);

    /**
     * 获取客户端的授权记录
     * 
     * @param clientId
     * @param refreshToken
     * @return
     */
    OAuth2AuthorizedClient getOAuth2AuthorizedClientByRefreshToken(String clientId, String refreshToken);

    /**
     * 获取客户端所有有效的授权记录
     * 
     * @param clientId
     * @return
     */
    List<OAuth2AuthorizedClient> getAllValidOAuth2AuthorizedClients(String clientId);

    /**
     * 移除所有授权的token
     * 
     * @param clientId
     * @return
     */
    boolean removeAccessTokens(String clientId);
}
