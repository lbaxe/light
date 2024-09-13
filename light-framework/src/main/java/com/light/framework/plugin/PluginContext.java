package com.light.framework.plugin;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.light.framework.plugin.controller.ControllerPlugin;
import com.light.framework.plugin.filter.ExternalFilterPlugin;
import com.light.framework.plugin.schedule.TaskPlugin;
import com.light.framework.util.AsynProcess;

public final class PluginContext {
    private final Map<Class<? extends IPlugin<?>>, IPlugin<?>> plugins = new LinkedHashMap<>();

    private final Class<? extends IPlugin<?>>[] pluginClassRegisters = new Class[] {

        ControllerPlugin.class, ExternalFilterPlugin.class,

        TaskPlugin.class};

    private static PluginContext instance = new PluginContext();

    public static PluginContext get() {
        return instance;
    }

    public synchronized void initialize() {
        Arrays.asList(this.pluginClassRegisters).forEach(clazz -> {
            loadPlugin(clazz);
        });
    }

    public synchronized void cancel() {
        AsynProcess asynProcess = new AsynProcess(this.plugins.size());
        for (Map.Entry<Class<? extends IPlugin<?>>, IPlugin<?>> entry : this.plugins.entrySet()) {
            asynProcess.execute(entry.getKey().getSimpleName(), () -> (entry.getValue()).destroy());
        }
        asynProcess.awaitAndfinish();
    }

    public <T extends IPlugin<?>> T getPlugin(Class<T> clazz) {
        return (T)this.plugins.get(clazz);
    }

    public <T extends IPlugin<?>> T loadPlugin(Class<T> clazz) {
        return (T)initPlugin(clazz);
    }

    private IPlugin<?> initPlugin(Class<? extends IPlugin<?>> clazz) {
        try {
            IPlugin<?> plugin = clazz.newInstance();
            plugin.init();
            this.plugins.put(clazz, plugin);
            return plugin;
        } catch (Exception e) {
            throw new RuntimeException("register plugin " + clazz + " fail.", e);
        }
    }
}
