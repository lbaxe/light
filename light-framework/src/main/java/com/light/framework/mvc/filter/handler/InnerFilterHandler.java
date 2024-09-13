package com.light.framework.mvc.filter.handler;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InnerFilterHandler implements FilterHandler {

    public InnerFilterHandler() {}

    @Override
    public boolean supports(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        return isMetrics(servletPath);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }

    public static boolean isMetrics(String servletPath) {
        return "/metrics".equals(servletPath) || "/metrics/".equals(servletPath);
    }
}