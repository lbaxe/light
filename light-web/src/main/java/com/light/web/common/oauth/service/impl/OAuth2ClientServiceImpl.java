package com.light.web.common.oauth.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.light.web.common.oauth.mapper.OAuth2AuthorizedClientMapper;
import com.light.web.common.oauth.mapper.OAuth2ClientMapper;
import com.light.web.common.oauth.mapper.entity.OAuth2AuthorizedClient;
import com.light.web.common.oauth.mapper.entity.OAuth2Client;
import com.light.web.common.oauth.service.OAuth2ClientService;

@Service
public class OAuth2ClientServiceImpl extends ServiceImpl<OAuth2ClientMapper, OAuth2Client>
    implements OAuth2ClientService {
    @Autowired
    private OAuth2AuthorizedClientMapper auth2AuthorizedClientMapper;

    @Override
    public OAuth2Client getOAuth2Client(String clientId) {
        return this.getBaseMapper().selectFirst(new LambdaQueryWrapper<OAuth2Client>()
            .eq(OAuth2Client::getClientId, clientId).orderByDesc(OAuth2Client::getId));
    }

    @Override
    public OAuth2AuthorizedClient getLatestOAuth2AuthorizedClientByClientId(String clientId) {
        return auth2AuthorizedClientMapper.selectFirst(new LambdaQueryWrapper<OAuth2AuthorizedClient>()
            .eq(OAuth2AuthorizedClient::getClientId, clientId).orderByDesc(OAuth2AuthorizedClient::getId));
    }

    @Override
    public OAuth2AuthorizedClient getOAuth2AuthorizedClientByAccessToken(String accessToken) {
        return auth2AuthorizedClientMapper.selectFirst(new LambdaQueryWrapper<OAuth2AuthorizedClient>()
            .eq(OAuth2AuthorizedClient::getAccessTokenValue, accessToken).orderByDesc(OAuth2AuthorizedClient::getId));
    }

    @Override
    public OAuth2AuthorizedClient getOAuth2AuthorizedClientByRefreshToken(String clientId, String refreshToken) {
        return auth2AuthorizedClientMapper.selectFirst(new LambdaQueryWrapper<OAuth2AuthorizedClient>()
            .eq(OAuth2AuthorizedClient::getClientId, clientId)
            .eq(OAuth2AuthorizedClient::getRefreshTokenValue, refreshToken).orderByDesc(OAuth2AuthorizedClient::getId));
    }
    @Override
    public List<OAuth2AuthorizedClient> getAllValidOAuth2AuthorizedClients(String clientId) {
        return auth2AuthorizedClientMapper.selectList(
            new LambdaQueryWrapper<OAuth2AuthorizedClient>().eq(OAuth2AuthorizedClient::getClientId, clientId)
                .orderByDesc(OAuth2AuthorizedClient::getId));
    }

    @Override
    public boolean removeAccessTokens(String clientId) {
        if (StringUtils.isBlank(clientId)) {
            return false;
        }
        return auth2AuthorizedClientMapper.delete(
            new LambdaQueryWrapper<OAuth2AuthorizedClient>().eq(OAuth2AuthorizedClient::getClientId, clientId)) > 0;
    }
}
