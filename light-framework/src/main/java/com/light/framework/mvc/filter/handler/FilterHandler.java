package com.light.framework.mvc.filter.handler;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.light.framework.mvc.filter.LightFilter;

public interface FilterHandler {
    Logger logger = LoggerFactory.getLogger(LightFilter.class);

    boolean supports(HttpServletRequest request);

    void handle(HttpServletRequest request, HttpServletResponse response, FilterChain proxyFilterChain)
        throws ServletException, IOException;
}
