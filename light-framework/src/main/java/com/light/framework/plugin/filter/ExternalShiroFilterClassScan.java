package com.light.framework.plugin.filter;

import java.lang.reflect.Modifier;

import com.light.framework.mvc.filter.shiro.ShiroProxyChainFilter;
import com.light.framework.plugin.AbstractClassScan;

public class ExternalShiroFilterClassScan extends AbstractClassScan {

    @Override
    protected String path() {
        return "com.dzq.**.filter.**";
    }

    @Override
    public boolean conventional(Class<?> clazz) {
        if (!ShiroProxyChainFilter.class.isAssignableFrom(clazz)) {
            return false;
        }
        if (!ShiroProxyChainFilter.class.isAssignableFrom(clazz)) {
            return false;
        }
        if (ShiroProxyChainFilter.class == clazz) {
            return false;
        }
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }
        // checkBean(clazz);
        return true;
    }
}
