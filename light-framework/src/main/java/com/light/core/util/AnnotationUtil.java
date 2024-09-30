package com.light.core.util;

import java.lang.reflect.Method;
import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

public class AnnotationUtil {
    public static String[] getRequestMapping(Class<?> controllerClass) {
        RequestMapping requestMapping = controllerClass.<RequestMapping>getAnnotation(RequestMapping.class);
        (new String[1])[0] = "";
        return (requestMapping != null) ? requestMapping.value() : new String[1];
    }

    public static String[] getRequestMapping(Method method) {
        RequestMapping requestMapping = method.<RequestMapping>getAnnotation(RequestMapping.class);
        if (Objects.isNull(requestMapping)) {
            GetMapping getMapping = method.<GetMapping>getAnnotation(GetMapping.class);
            if (Objects.isNull(getMapping)) {
                PostMapping postMapping = method.<PostMapping>getAnnotation(PostMapping.class);
                (new String[1])[0] = "";
                return (postMapping != null) ? postMapping.value() : new String[1];
            }
            return getMapping.value();
        }
        return requestMapping.value();
    }
}