package com.light.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import org.springframework.web.util.NestedServletException;

public class ThrowableUtil {
    public static Throwable unwrapThrowable(Throwable wrap) {
        Throwable actual = wrap;
        while (true) {
            while (actual instanceof NestedServletException) {
                actual = actual.getCause();
            }
            if (actual instanceof InvocationTargetException) {
                actual = ((InvocationTargetException)actual).getTargetException();
                continue;
            }
            if (actual instanceof UndeclaredThrowableException) {
                actual = ((UndeclaredThrowableException)actual).getUndeclaredThrowable();
                continue;
            }
            break;
        }
        return actual;
    }
}