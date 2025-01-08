package com.light.web.common.shiro.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.web.servlet.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import com.light.framework.mvc.filter.shiro.ShiroGlobalFilter;

@Component("UserContextFilter")
public class UserContextFilter extends OncePerRequestFilter implements ShiroGlobalFilter {

    @Override
    protected void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        chain.doFilter(request, response);
    }
}
