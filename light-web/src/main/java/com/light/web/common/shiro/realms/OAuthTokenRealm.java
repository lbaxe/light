package com.light.web.common.shiro.realms;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import com.light.web.common.oauth.OAuth2AccessToken;

/**
 * token Realm
 */
@Component("OAuthTokenRealm")
public class OAuthTokenRealm extends AbstractAuthzAndAuthcRealm implements Realm {

    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && token instanceof OAuth2AccessToken;
    }

    /**
     * 执行认证逻辑
     *
     * @param authenticationToken
     * @return
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) {
        OAuth2AccessToken token = (OAuth2AccessToken)authenticationToken;
        return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), getName());
    }

    @Override
    protected void clearCachedAuthenticationInfo(PrincipalCollection principals) {
        super.clearCachedAuthenticationInfo(principals);
    }
}
