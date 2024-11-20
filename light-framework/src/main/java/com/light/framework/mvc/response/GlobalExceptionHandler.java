package com.light.framework.mvc.response;

import java.sql.SQLException;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.light.core.exception.ServiceException;
import com.light.core.log.DebugLogger;
import com.light.framework.cache.exception.CacheException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static DebugLogger debugLogger = DebugLogger.getInstance();
    private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public JsonResult handleException(ServiceException e) {
        // logger.info(e.message(), e);
        return JsonResult.error(e.code(), e.message());
    }

    @ExceptionHandler(SQLException.class)
    @ResponseBody
    public JsonResult handleException(SQLException e) {
        logger.error(e.getMessage(), e);
        return JsonResult.error("系统异常");
    }

    @ExceptionHandler(CacheException.class)
    @ResponseBody
    public JsonResult handleException(CacheException e) {
        // logger.error(e.getMessage(), e);
        return JsonResult.error("系统异常");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public JsonResult handleException(ConstraintViolationException e) {
        // logger.error(e.getMessage(), e);
        Set<ConstraintViolation<?>> set = e.getConstraintViolations();
        String msg = e.getMessage();
        if (!CollectionUtils.isEmpty(set)) {
            ConstraintViolation constraintViolation = set.iterator().next();
            if (constraintViolation != null) {
                msg = constraintViolation.getMessage();
            }
        }
        return JsonResult.error(msg);
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public JsonResult handleException(BindException e) {
        // logger.error(e.getMessage(), e);
        return JsonResult.error(e.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public JsonResult handleException(Exception e) {
        // logger.error(e.getMessage(), e);
        return JsonResult.error("系统异常");
    }
}