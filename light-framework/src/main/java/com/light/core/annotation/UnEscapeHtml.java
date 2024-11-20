package com.light.core.annotation;

import com.light.framework.mvc.filter.light.inner.XssFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * controller层参数注释， 注释参数将被{@link XssFilter}<br>
 * 忽略处理， 注释参数包含的html代码不会被转义。
 * 
 * @see XssFilter
 * @author luban
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UnEscapeHtml {}