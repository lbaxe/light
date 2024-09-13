package com.light.framework.mvc.config.extend;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.light.framework.util.UrlUtil;
import org.springframework.aop.support.AopUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class RpcRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    @Override
    protected boolean isHandler(Class<?> beanType) {
        boolean isRpcService = Arrays.stream(beanType.getInterfaces())
            .anyMatch(e -> AnnotatedElementUtils.hasAnnotation(e, FeignClient.class));
        if (isRpcService) {
            return true;
        }
        return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class)
            || AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
    }

    @Override
    protected RequestMappingInfo createRequestMappingInfo(RequestMapping requestMapping,
        @Nullable RequestCondition<?> customCondition) {
        RequestMappingInfo.Builder builder = RequestMappingInfo
            .paths(resolveEmbeddedValuesInPatterns(requestMapping.path())).methods(requestMapping.method())
            .params(requestMapping.params()).headers(requestMapping.headers()).consumes(requestMapping.consumes())
            .produces(requestMapping.produces()).mappingName(requestMapping.name());
        if (customCondition != null) {
            builder.customCondition(customCondition);
        }

        return builder.options(this.getBuilderConfiguration()).build();
    }

    @Override
    protected void detectHandlerMethods(Object handler) {
        Class<?> handlerType =
            (handler instanceof String ? obtainApplicationContext().getType((String)handler) : handler.getClass());

        if (handlerType != null) {
            Class<?> userType = ClassUtils.getUserClass(handlerType);
            Map<Method, RequestMappingInfo> methods = MethodIntrospector.selectMethods(userType,
                (MethodIntrospector.MetadataLookup<RequestMappingInfo>)method -> {
                    try {
                        return this.getMappingForMethod(method, userType);
                    } catch (Throwable ex) {
                        throw new IllegalStateException(
                            "Invalid mapping on handler class [" + userType.getName() + "]: " + method, ex);
                    }
                });
            if (logger.isTraceEnabled()) {
                logger.trace(formatMappings(userType, methods));
            } else if (mappingsLogger.isDebugEnabled()) {
                mappingsLogger.debug(formatMappings(userType, methods));
            }
            methods.forEach((method, mapping) -> {
                List<Class<?>> rpcServices = Arrays.stream(method.getDeclaringClass().getInterfaces())
                    .filter(e -> AnnotatedElementUtils.hasAnnotation(e, FeignClient.class))
                    .collect(Collectors.toList());
                if (rpcServices.isEmpty()) {
                    Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
                    registerHandlerMethod(handler, invocableMethod, mapping);
                } else {
                    Class<?> interfaceHandlerType = rpcServices.get(0);
                    Method interfaceMethod = null;
                    try {
                        interfaceMethod = interfaceHandlerType.getMethod(method.getName(), method.getParameterTypes());
                        Method invocableMethod = AopUtils.selectInvocableMethod(interfaceMethod, interfaceHandlerType);
                        registerHandlerMethod(handler, invocableMethod, mapping);
                    } catch (NoSuchMethodException e) {

                    }
                }

            });
        }
    }

    private String formatMappings(Class<?> userType, Map<Method, RequestMappingInfo> methods) {
        String packageName = ClassUtils.getPackageName(userType);
        String formattedType = (StringUtils.hasText(packageName)
            ? Arrays.stream(packageName.split("\\.")).map(packageSegment -> packageSegment.substring(0, 1))
                .collect(Collectors.joining(".", "", "." + userType.getSimpleName()))
            : userType.getSimpleName());
        Function<Method, String> methodFormatter = method -> Arrays.stream(method.getParameterTypes())
            .map(Class::getSimpleName).collect(Collectors.joining(",", "(", ")"));
        return methods.entrySet().stream().map(e -> {
            Method method = e.getKey();
            return e.getValue() + ": " + method.getName() + methodFormatter.apply(method);
        }).collect(Collectors.joining("\n\t", "\n\t" + formattedType + ":" + "\n\t", ""));
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        List<Class<?>> rpcServices = Arrays.stream(method.getDeclaringClass().getInterfaces())
            .filter(e -> AnnotatedElementUtils.hasAnnotation(e, FeignClient.class)).collect(Collectors.toList());
        if (rpcServices.isEmpty()) {
            return super.getMappingForMethod(method, handlerType);
        }
        Class<?> interfaceHandlerType = rpcServices.get(0);
        Method interfaceMethod = null;
        try {
            interfaceMethod = interfaceHandlerType.getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return null;
        }
        RequestMappingInfo info = createRequestMappingInfo(interfaceMethod);
        RequestMappingInfo typeInfo = createRequestMappingInfo(interfaceHandlerType);
        if (info != null) {
            if (typeInfo != null) {
                info = typeInfo.combine(info);
            }
        }
        return info;
    }

    private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        RequestCondition<?> condition = (element instanceof Class ? getCustomTypeCondition((Class<?>)element)
            : getCustomMethodCondition((Method)element));
        if (requestMapping != null) {
            return createRequestMappingInfo(requestMapping, condition);
        }
        return createDefaultRequestMappingInfo(element, condition);
    }

    private RequestMappingInfo createDefaultRequestMappingInfo(AnnotatedElement element,
        RequestCondition<?> condition) {
        String path = element instanceof Class ? UrlUtil.getNamespace4API() + ((Class<?>)element).getSimpleName() + "/"
            : ((Method)element).getName();
        RequestMapping requestMapping = new RequestMapping() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return RequestMapping.class;
            }

            @Override
            public String name() {
                return "";
            }

            @Override
            public String[] value() {
                return new String[] {path};
            }

            @Override
            public String[] path() {
                return value();
            }

            @Override
            public RequestMethod[] method() {
                return new RequestMethod[] {RequestMethod.POST};
            }

            @Override
            public String[] params() {
                return new String[0];
            }

            @Override
            public String[] headers() {
                return new String[0];
            }

            @Override
            public String[] consumes() {
                return new String[] {MediaType.APPLICATION_FORM_URLENCODED_VALUE};
            }

            @Override
            public String[] produces() {
                return new String[] {MediaType.APPLICATION_JSON_VALUE};
            }
        };

        return this.createRequestMappingInfo(requestMapping, condition);
    }
}
