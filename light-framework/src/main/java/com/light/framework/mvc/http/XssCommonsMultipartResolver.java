package com.light.framework.mvc.http;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.util.HtmlUtils;

import com.light.framework.plugin.PluginContext;
import com.light.framework.plugin.controller.ControllerPlugin;

public class XssCommonsMultipartResolver extends CommonsMultipartResolver {

    @Override
    protected MultipartParsingResult parseRequest(HttpServletRequest request) throws MultipartException {
        ControllerPlugin controllerPlugin = PluginContext.get().getPlugin(ControllerPlugin.class);

        request.getParameterMap().entrySet().stream()
            .forEach(entry -> System.out.println(entry.getKey() + ":" + entry.getValue()));

        MultipartParsingResult parseRequest = super.parseRequest(request);
        Map<String, String[]> multipartParameters = parseRequest.getMultipartParameters();
        Set<String> paramNames = controllerPlugin.getUnEscapeHtmlControllerParam(request.getServletPath());
        if (multipartParameters != null) {
            for (Map.Entry<String, String[]> entry : multipartParameters.entrySet()) {
                String name = entry.getKey();
                String[] value = entry.getValue();
                if (isWhitelist(name, paramNames)) {
                    multipartParameters.put(name, value);
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
                multipartParameters.put(name, escapeArray);
            }
        }
        System.out.println("============================================================");
        multipartParameters.entrySet().stream()
            .forEach(entry -> System.out.println(entry.getKey() + ":" + entry.getValue()));
        return parseRequest;
    }

    private boolean isWhitelist(String name, Set<String> paramNames) {
        return (paramNames != null) ? paramNames.contains(name) : false;
    }

    private String escape(String value) {
        // apache会对中文进行转义，改为springframework工具类
        // StringEscapeUtils.escapeHtml(value);
        return value == null ? null : HtmlUtils.htmlEscape(value);
    }
}
