package com.light.framework.mvc.filter.light.handler;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.light.framework.mvc.filter.light.GenericFilterBean;
import com.light.framework.mvc.filter.light.ProxyFilterChain;

public class OptionsFilterHandler implements FilterHandler {
    private List<GenericFilterBean> filters;

    public OptionsFilterHandler(List<GenericFilterBean> filters) {
        this.filters = filters;
    }

    @Override
    public boolean supports(HttpServletRequest request) {
        return "OPTIONS".equals(request.getMethod());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        ProxyFilterChain pfc = new ProxyFilterChain(filterChain, filters);
        pfc.doFilter(request, response);
    }
}