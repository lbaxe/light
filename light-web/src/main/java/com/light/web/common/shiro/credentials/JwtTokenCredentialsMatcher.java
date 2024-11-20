package com.light.web.common.shiro.credentials;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;

import com.dzq.portal.web.common.shiro.token.JwtToken;

/**
 * 自定义本地的token凭证匹配器 这个方法很重要，决定了基于token验证的方案的成败 即：doGetAuthenticationInfo 执行的成败，如果失败了，结果会失败，内容未知（也可能没任何数据）
 * AuthenticatingRealm - assertCredentialsMatch 方法会具体使用
 *
 * @author chenlk
 */
public class JwtTokenCredentialsMatcher implements CredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        if (token instanceof JwtToken) {
            return true;
        }

        return false;
    }
}
