package com.light.framework.mvc.filter.light.inner;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.filter.AbstractRequestLoggingFilter;

public class RequestLoggingFilter extends AbstractRequestLoggingFilter {

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        // log.info(message);
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        // log.info(message);
    }
}
