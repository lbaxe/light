package com.light.framework.plugin.schedule;

import java.lang.reflect.Modifier;

import com.light.framework.plugin.AbstractClassScan;
import com.light.framework.quartz.task.ITask;

public class TaskClassScan extends AbstractClassScan {

    @Override
    protected String path() {
        return "com.light.**.task.**";
    }

    @Override
    public boolean conventional(Class<?> clazz) {
        if (!ITask.class.isAssignableFrom(clazz)) {
            return false;
        }
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }
        if (Modifier.isInterface(clazz.getModifiers())) {
            return false;
        }
        checkBean(clazz);
        return true;
    }
}
