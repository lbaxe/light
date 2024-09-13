package com.light.framework.plugin.controller;

import java.lang.reflect.Modifier;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import com.light.framework.plugin.AbstractClassScan;

public class ControllerClassScan extends AbstractClassScan {

    @Override
    protected String path() {
        return "com.light.**.controller.**";
    }

    @Override
    public boolean conventional(Class<?> clazz) {
        if (clazz.getAnnotation(Controller.class) == null && clazz.getAnnotation(RestController.class) == null) {
            return false;
        }
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }
        checkBean(clazz);
        return true;
    }
}
