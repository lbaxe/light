package com.light.framework.mvc.filter.light.external;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.core.annotation.Order;

import com.light.core.exception.ServiceException;
import com.light.framework.mvc.filter.light.GenericFilterBean;

@Order
public abstract class ExternalFilter extends GenericFilterBean implements OrderedFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        try {
            if (preDoFilter(request, response)) {
                filterChain.doFilter(request, response);
            }
        } finally {
            afterDoFilter(request, response);
        }
    }

    @Override
    protected String urlPattern() {
        return "/*";
    }

    protected abstract boolean preDoFilter(HttpServletRequest request, HttpServletResponse response)
        throws ServiceException;

    /**
     * doFilter之后do something
     * 
     * @param request
     * @param response
     * @throws ServiceException
     */
    protected void afterDoFilter(HttpServletRequest request, HttpServletResponse response) throws ServiceException {}

    @Override
    public int getOrder() {
        return REQUEST_WRAPPER_FILTER_MAX_ORDER;
    }
}