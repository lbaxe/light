package com.light.web.common.shiro.realms;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.springframework.stereotype.Component;

/**
 * 用户名密码Realm
 */
@Component("UsernamePasswordRealm")
public class UsernamePasswordRealm extends AbstractAuthzAndAuthcRealm implements Realm {

    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && token instanceof UsernamePasswordToken;
    }

    /**
     * 执行认证逻辑 ·
     *
     * @param authenticationToken
     * @return
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) {

        return null;
    }
}
