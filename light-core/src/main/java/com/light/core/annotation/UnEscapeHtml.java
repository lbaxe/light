package com.light.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description 不做xss判断参数
 * 
 * @param: null
 * @date 2022/7/27 13:31
 * @author luban
 * @version 1.0
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UnEscapeHtml {}