package com.light.framework.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReflectUtil {
    public static Map<String, Object> getGetterFieldValues(Object bean) {
        if (bean == null) {
            return Collections.emptyMap();
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            Map<String, Object> jobInitParamMap = new HashMap<>();
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
                if ("class".equals(descriptor.getName())) {
                    continue;
                }
                Object value = descriptor.getReadMethod().invoke(bean, null);
                jobInitParamMap.put(descriptor.getName(), value);
            }
            return jobInitParamMap;
        } catch (Exception e) {
            throw new RuntimeException("获取bean参数信息异常", e);
        }
    }

    public static void setSetterFieldValues(Object bean, Map<String, Object> fieldValueMap) {
        if (bean == null) {
            return;
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            Map<String, Object> jobInitParamMap = new HashMap<>();
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
                if ("class".equals(descriptor.getName())) {
                    continue;
                }
                if (!fieldValueMap.containsKey(descriptor.getName())) {
                    continue;
                }
                descriptor.getWriteMethod().invoke(bean, new Object[] {fieldValueMap.get(descriptor.getName())});
            }
        } catch (Exception e) {
            throw new RuntimeException("设置bean参数信息异常", e);
        }
    }
}
