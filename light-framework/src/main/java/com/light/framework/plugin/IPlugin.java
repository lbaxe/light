package com.light.framework.plugin;

public interface IPlugin<T> {
    void init();

    void destroy();

    T getObject();
}
