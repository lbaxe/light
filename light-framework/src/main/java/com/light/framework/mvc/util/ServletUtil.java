package com.light.framework.mvc.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ServletUtil {
    private static final Logger logger = LoggerFactory.getLogger(ServletUtil.class);

    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            throw new RuntimeException("不支持的请求");
        }
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)requestAttributes;
        return servletRequestAttributes.getRequest();
    }

    public static HttpServletResponse getResponse() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            throw new RuntimeException("不支持的请求");
        }
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)requestAttributes;
        return servletRequestAttributes.getResponse();
    }
}
