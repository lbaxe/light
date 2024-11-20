package com.light.web.common.shiro.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.light.framework.mvc.filter.shiro.external.ExternalShiroFilter;

/**
 * token请求认证过滤器
 */
@Component("JwtFilter")
public class JwtFilter extends BasicHttpAuthenticationFilter implements ExternalShiroFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public int priority() {
        return 3;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 非jwt请求,兼容eos token请求，eos请求jwtFilter不做任何处理
        if (!isLoginAttempt(request, response)) {
            return true;
        }
        Subject subject = this.getSubject(request, response);
        return subject.isAuthenticated() && subject.getPrincipal() != null;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        return true;
    }

    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        return true;
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        return true;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        return null;
    }
}
