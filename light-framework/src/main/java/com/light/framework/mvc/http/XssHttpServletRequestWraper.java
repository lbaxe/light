package com.light.framework.mvc.http;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.web.util.HtmlUtils;

public class XssHttpServletRequestWraper extends HttpServletRequestWrapper {
    private final Set<String> paramNamesWhitelist;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public XssHttpServletRequestWraper(HttpServletRequest request, Set<String> paramNames) {
        super(request);
        this.paramNamesWhitelist = paramNames;
    }

    private String escape(String value) {
        // apache会对中文进行转义，改为springframework工具类
        // StringEscapeUtils.escapeHtml(value);
        return value == null ? null : HtmlUtils.htmlEscape(value);
    }

    private boolean isWhitelist(String name) {
        return this.paramNamesWhitelist != null ? this.paramNamesWhitelist.contains(name) : false;
    }

    @Override
    public String getHeader(String name) {
        return this.escape(super.getHeader(name));
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        Enumeration<String> headers = super.getHeaders(name);
        if (headers == null) {
            return null;
        }
        Vector<String> vector = new Vector<>();
        while (headers.hasMoreElements()) {
            vector.add(escape(headers.nextElement()));
        }
        return vector.elements();
    }

    @Override
    public String getQueryString() {
        return escape(super.getQueryString());
    }

    @Override
    public String getParameter(String name) {
        return isWhitelist(name) ? super.getParameter(name) : escape(super.getParameter(name));
    }

    @Override
    public Map getParameterMap() {
        Map<String, String[]> parameterMap = super.getParameterMap();
        if (parameterMap == null) {
            return null;
        }
        Map<String, String[]> escapeMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String name = entry.getKey();
            String[] value = entry.getValue();
            if (isWhitelist(name)) {
                escapeMap.put(name, value);
                continue;
            }

            String[] escapeArray = null;
            if (value != null && value.length != 0) {
                if (value.length == 1) {
                    escapeArray = new String[1];
                    escapeArray[0] = escape(value[0]);
                } else {
                    escapeArray = new String[value.length];
                    for (int i = 0; i < value.length; i++) {
                        escapeArray[i] = escape(value[i]);
                    }
                }
            }
            escapeMap.put(name, escapeArray);
        }
        return escapeMap;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] parameterValues = super.getParameterValues(name);
        if (!isWhitelist(name) && parameterValues != null) {
            String[] escapeValues = new String[parameterValues.length];
            for (int i = 0; i < parameterValues.length; i++) {
                escapeValues[i] = escape(parameterValues[i]);
            }
            return escapeValues;
        }
        return parameterValues;
    }
}
