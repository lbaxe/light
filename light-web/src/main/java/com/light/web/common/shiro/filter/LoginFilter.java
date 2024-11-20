package com.light.web.common.shiro.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.light.framework.mvc.filter.shiro.external.ExternalShiroFilter;

/**
 * login请求认证过滤器
 */
@Service("LoginFilter")
public class LoginFilter extends BasicHttpAuthenticationFilter implements ExternalShiroFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String urlPattern() {
        return "/login/login";
    }

    @Override
    public int priority() {
        return 10;
    }

    /**
     * 判断是否已经登录，因为是基于token的验证，非session的验证方式，所以此方法跳过方法默认返回false
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        return false;
    }

    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        String uri = httpServletRequest.getRequestURI();
        return uri.equals(urlPattern());
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) {
        return false;

    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        String host = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return new UsernamePasswordToken(request.getParameter("username"), request.getParameter("password"), host);
    }
}
