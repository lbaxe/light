package com.light.framework.mvc.response;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.light.core.annotation.IgnoreResponseWrapper;
import com.light.core.exception.ServiceException;
import com.light.core.util.ThrowableUtil;
import com.light.framework.mvc.util.RequestUtil;

@ControllerAdvice
public class GlobalControllerResponseBodyAdvice implements ResponseBodyAdvice {
    private static Logger logger = LoggerFactory.getLogger(GlobalControllerResponseBodyAdvice.class);

    private static ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setFilters(new SimpleFilterProvider().setFailOnUnknownId(false));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return (returnType.hasMethodAnnotation(ResponseBody.class)
            || returnType.getDeclaringClass().getAnnotation(RestController.class) != null)
            && (!returnType.hasMethodAnnotation(IgnoreResponseWrapper.class)
                && returnType.getDeclaringClass().getAnnotation(IgnoreResponseWrapper.class) == null);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
        Class selectedConverterType, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        String callback = null;
        HttpServletRequest request = null;
        if (serverHttpRequest instanceof ServletServerHttpRequest) {
            request = ((ServletServerHttpRequest)serverHttpRequest).getServletRequest();
            callback = request.getParameter("callback");
        }
        Object newBody = body;
        if (returnType.getNestedParameterType().isAssignableFrom(ResponseEntity.class)) {
            if (body instanceof Map) {
                Map mapBody = (Map)body;
                if (serverHttpResponse instanceof ServletServerHttpResponse) {
                    int status = ((ServletServerHttpResponse)serverHttpResponse).getServletResponse().getStatus();
                    Throwable t = (Throwable)mapBody.get("exp");
                    Throwable actual = ThrowableUtil.unwrapThrowable(t);
                    if (actual instanceof ServiceException) {
                        newBody = AjaxResult.error(((ServiceException)actual).code(), actual.getMessage());
                    } else {
                        newBody = AjaxResult.error(status + "", mapBody.get("error") + "");
                    }
                }
            } else {
                // do nothing
                newBody = AjaxResult.error("系统异常，请联系管理员");
            }
        } else {
            if (body instanceof String) {
                try {
                    newBody = objectMapper.writeValueAsString(AjaxResult.success(body));
                } catch (JsonProcessingException e) {
                    logger.error(
                        serverHttpRequest.getURI().getPath() + " ,request uri path: {}, format response body error", e);
                }
            } else if (!(body instanceof AjaxResult)) {
                if (serverHttpResponse instanceof ServletServerHttpResponse) {
                    int status = ((ServletServerHttpResponse)serverHttpResponse).getServletResponse().getStatus();
                    if (status == HttpStatus.OK.value()) {
                        newBody = AjaxResult.success(body != null ? body : Collections.emptyMap());
                    } else {
                        newBody = AjaxResult.error(status + "", body.toString());
                    }
                } else {
                    newBody = AjaxResult.success(body != null ? body : Collections.emptyMap());
                }
            }
        }

        Object result = callback == null || "".equals(callback) ? newBody : callback + "(" + newBody + ")";
        // 添加返回结果
        RequestUtil.setOutput(request, result);
        return result;
    }

}