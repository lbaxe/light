package com.light.framework.plugin.filter;

import java.lang.reflect.Modifier;

import com.light.framework.mvc.filter.external.ExternalFilter;
import com.light.framework.plugin.AbstractClassScan;

public class ExternalFilterClassScan extends AbstractClassScan {

    @Override
    protected String path() {
        return "com.light.**.filter.**";
    }

    @Override
    public boolean conventional(Class<?> clazz) {
        if (!ExternalFilter.class.isAssignableFrom(clazz)) {
            return false;
        }
        if (ExternalFilter.class == clazz) {
            return false;
        }
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }
        // checkBean(clazz);
        return true;
    }
}
