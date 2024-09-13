package com.light.framework.mvc.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

public class RequestUtil {
    private static final ConcurrentHashMap<String, Boolean> clazzMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Boolean> methodMap = new ConcurrentHashMap<>();

    public static boolean isAjaxRequest(HttpServletRequest request) {
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return true;
        }
        if (request.getParameter("callback") != null) {
            return true;
        }
        return false;
    }

    public static boolean isGzipRequest(HttpServletRequest request) {
        String header = request.getHeader("accept-encoding");
        return header != null && header.toLowerCase().indexOf("gzip") != -1;
    }

    public static boolean isJsonRequest(RequestMappingHandlerMapping requestMappingHandlerMapping,
        HttpServletRequest request) {
        if (requestMappingHandlerMapping == null) {
            return false;
        }
        HandlerExecutionChain handlerExecutionChain = null;
        try {
            ServletRequestPathUtils.parseAndCache(request);
            handlerExecutionChain = requestMappingHandlerMapping.getHandler(request);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (handlerExecutionChain != null) {
            Object handlerObject = handlerExecutionChain.getHandler();
            if (handlerObject instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod)handlerObject;
                String controllerClazzName = handlerMethod.getBeanType().getCanonicalName();
                Boolean isExist = clazzMap.get(controllerClazzName);
                if (isExist != null && isExist == true) {
                    return true;
                }
                ResponseBody classResponseBody =
                        AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), ResponseBody.class);
                clazzMap.put(handlerMethod.getBeanType().getCanonicalName(), classResponseBody != null);
                if (classResponseBody != null) {
                    return true;
                }
                String methodName = controllerClazzName + "." + handlerMethod.getMethod().getName();
                isExist = methodMap.get(methodName);
                if (isExist != null && isExist == true) {
                    return true;
                }
                ResponseBody methodResponseBody =
                        AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), ResponseBody.class);
                methodMap.put(
                        handlerMethod.getBeanType().getCanonicalName() + "." + handlerMethod.getMethod().getName(),
                        methodResponseBody != null);
                if (methodResponseBody != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Map<String, Object> getInput(HttpServletRequest request) {
        return (Map<String, Object>)request.getAttribute("input");
    }

    public static Object getOutput(HttpServletRequest request) {
        return request.getAttribute("output");
    }

    public static void setInput(HttpServletRequest request, Map<String, Object> input) {
        request.setAttribute("input", input);
    }

    public static void setOutput(HttpServletRequest request, Object output) {
        request.setAttribute("output", output);
    }

    public static void clean(HttpServletRequest request) {
        request.removeAttribute("input");
        request.removeAttribute("output");
    }

    public static Map<String, Object> getParameters(HttpServletRequest request) {
        Set<Map.Entry<String, String[]>> set = (Set)request.getParameterMap().entrySet();
        Map<String, Object> inputMap = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : set) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            if (value != null && value.length != 0) {
                if (value.length == 1) {
                    inputMap.put(key, value[0]);
                    continue;
                }
                inputMap.put(key, Arrays.asList(value));
            }
        }
        return inputMap;
    }
}
