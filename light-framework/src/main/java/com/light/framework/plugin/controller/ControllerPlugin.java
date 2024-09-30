package com.light.framework.plugin.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import com.light.core.annotation.UnEscapeHtml;
import com.light.core.util.AnnotationUtil;
import com.light.framework.plugin.IPlugin;

public class ControllerPlugin implements IPlugin<Map<String, Set<String>>> {
    private static final Logger logger = LoggerFactory.getLogger(ControllerPlugin.class);

    private final LocalVariableTableParameterNameDiscoverer classPathDiscoverer =
        new LocalVariableTableParameterNameDiscoverer();
    private final Map<String, Set<String>> unEscapeHtmlControllerParamMap = new HashMap<>();
    private ControllerClassScan controllerClassScan;

    public ControllerPlugin() {
        this.controllerClassScan = new ControllerClassScan();
    }

    @Override
    public void init() {
        List<Class<?>> controllerClasses = this.controllerClassScan.scan();
        for (Class clazz : controllerClasses) {
            Method[] methods = clazz.getMethods();
            String[] classRequestMappings = AnnotationUtil.getRequestMapping(clazz);

            for (Method method : methods) {
                String[] parameterNames = this.classPathDiscoverer.getParameterNames(method);
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                String[] methodRequestMappings = AnnotationUtil.getRequestMapping(method);

                Set<String> unEscapeHtmlParamNames = new LinkedHashSet<>();

                for (int i = 0; i < parameterAnnotations.length; i++) {
                    Annotation[] annotations = parameterAnnotations[i];
                    for (int j = 0; j < annotations.length; j++) {
                        Annotation annotation = annotations[j];
                        if (annotation instanceof UnEscapeHtml) {
                            unEscapeHtmlParamNames.add(parameterNames[i]);
                        }
                    }
                }

                for (String classRequestMapping : classRequestMappings) {
                    for (String methodRequestMapping : methodRequestMappings) {
                        if (unEscapeHtmlParamNames.size() > 0) {
                            this.unEscapeHtmlControllerParamMap.put(classRequestMapping + methodRequestMapping,
                                unEscapeHtmlParamNames);
                        }
                    }
                }
            }
        }
        logger.info("ControllerPlugin init.");
    }

    public Set<String> getUnEscapeHtmlControllerParam(String methodUri) {
        return this.unEscapeHtmlControllerParamMap.get(methodUri);
    }

    @Override
    public void destroy() {
        unEscapeHtmlControllerParamMap.clear();
        logger.info("ControllerPlugin destroy");
    }

    @Override
    public Map<String, Set<String>> getObject() {
        return Collections.unmodifiableMap(this.unEscapeHtmlControllerParamMap);
    }
}
