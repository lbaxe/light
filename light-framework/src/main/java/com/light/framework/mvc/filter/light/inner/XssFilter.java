package com.light.framework.mvc.filter.light.inner;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.light.framework.mvc.filter.light.GenericFilterBean;
import com.light.framework.mvc.http.XssHttpServletRequestWraper;
import com.light.framework.plugin.PluginContext;
import com.light.framework.plugin.controller.ControllerPlugin;

/**
 * xss防脚本注入
 */
public class XssFilter extends GenericFilterBean {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        ControllerPlugin controllerPlugin = PluginContext.get().getPlugin(ControllerPlugin.class);

        XssHttpServletRequestWraper xssHttpServletRequestWraper = new XssHttpServletRequestWraper(request,
            controllerPlugin.getUnEscapeHtmlControllerParam(request.getServletPath()));
        filterChain.doFilter(xssHttpServletRequestWraper, response);
    }

    @Override
    protected String urlPattern() {
        return "/*";
    }
}
