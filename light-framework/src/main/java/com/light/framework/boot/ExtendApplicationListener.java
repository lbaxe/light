package com.light.framework.boot;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.light.framework.plugin.PluginContext;

@Component
public class ExtendApplicationListener implements ApplicationListener<ApplicationEvent> {
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            PluginContext.get().initialize();
        }
        if (event instanceof ContextClosedEvent) {
            PluginContext.get().cancel();
        }
    }
}
