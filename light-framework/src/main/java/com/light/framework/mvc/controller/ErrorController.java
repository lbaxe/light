package com.light.framework.mvc.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

import com.light.core.log.DebugLogger;

public class ErrorController extends BasicErrorController {
    private DebugLogger debugLogger = DebugLogger.getInstance();
    private final ErrorAttributes errorAttributes;

    public ErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties,
        List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorProperties, errorViewResolvers);
        this.errorAttributes = errorAttributes;
    }

    @Override
    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = getStatus(request);
        Map<String, Object> model = Collections
            .unmodifiableMap(getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.TEXT_HTML)));
        response.setStatus(status.value());
        ModelAndView modelAndView = resolveErrorView(request, response, status, model);
        return (modelAndView != null) ? modelAndView : new ModelAndView("error", model);
    }

    @Override
    @RequestMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if (status == HttpStatus.NO_CONTENT) {
            return new ResponseEntity<>(status);
        }
        // Map<String, Object> body = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        Map<String,
            Object> body = getErrorAttributes(request,
                ErrorAttributeOptions.defaults().including(ErrorAttributeOptions.Include.EXCEPTION,
                    ErrorAttributeOptions.Include.MESSAGE, ErrorAttributeOptions.Include.BINDING_ERRORS));
        // body.remove("timestamp");
        // body.remove("status");
        // body.remove("error");
        // body.remove("exception");
        // body.remove("path");
        // String messageCode = (String) attributes.get("message");
        Throwable t = errorAttributes.getError(new ServletWebRequest(request));
        if (t == null) {
            debugLogger.log(request.getServletPath() + "异常,body = " + body.toString());
        } else {
            debugLogger.log(request.getServletPath() + "异常," + t.getMessage(), t);
        }
        body.put("exp", t);
        return new ResponseEntity<>(body, status);
    }
}
