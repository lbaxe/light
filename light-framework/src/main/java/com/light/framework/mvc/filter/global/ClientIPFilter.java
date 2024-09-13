package com.light.framework.mvc.filter.global;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.light.core.util.ThreadLocalUtil;
import com.light.framework.mvc.filter.GenericFilterBean;
import com.light.framework.util.IPUtil;

public class ClientIPFilter extends GenericFilterBean {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        ThreadLocalUtil.getInstance().setClientIP(IPUtil.getIpAddr(request));
        filterChain.doFilter(request, response);
    }

    @Override
    protected String urlPattern() {
        return "/*";
    }
}