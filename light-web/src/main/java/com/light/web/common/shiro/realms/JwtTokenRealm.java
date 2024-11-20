package com.light.web.common.shiro.realms;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import com.light.framework.mvc.filter.shiro.token.JwtToken;

/**
 * token Realm
 */
@Component("JwtTokenRealm")
public class JwtTokenRealm extends AbstractAuthzAndAuthcRealm implements Realm {

    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && token instanceof JwtToken;
    }

    /**
     * 执行认证逻辑
     *
     * @param authenticationToken
     * @return
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) {
        return null;
    }

    @Override
    protected void clearCachedAuthenticationInfo(PrincipalCollection principals) {

    }
}
