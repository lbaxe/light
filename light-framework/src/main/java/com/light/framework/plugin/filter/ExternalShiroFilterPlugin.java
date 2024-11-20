package com.light.framework.plugin.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.light.framework.mvc.filter.shiro.external.ExternalShiroFilter;
import com.light.framework.plugin.IPlugin;

public class ExternalShiroFilterPlugin implements IPlugin<List<ExternalShiroFilter>> {
    private static final Logger logger = LoggerFactory.getLogger(ExternalShiroFilterPlugin.class);

    private final List<ExternalShiroFilter> filters = new ArrayList<>();

    private ExternalShiroFilterClassScan externalShiroFilterClassScan;

    public ExternalShiroFilterPlugin() {
        this.externalShiroFilterClassScan = new ExternalShiroFilterClassScan();
    }

    @Override
    public void init() {
        StringBuilder log = new StringBuilder();
        List<Class<?>> list = this.externalShiroFilterClassScan.scan();
        for (Class<?> clazz : list) {
            try {
                this.filters.add((ExternalShiroFilter)clazz.newInstance());
            } catch (Exception e) {
                logger.error("external filter load fail", e);
            }
            log.append("Added {" + clazz.getName()).append("} to requestInterceptor\n");
        }
        Collections.sort(this.filters, (o1, o2) -> {
            if (o1.priority() == o2.priority()) {
                return 0;
            }
            return (o1.priority() > o2.priority()) ? -1 : 1;
        });
        logger.info("ExternalShiroFilterPlugin init.");
        logger.info(log.toString());
    }

    @Override
    public void destroy() {
        this.filters.clear();
        logger.info("ExternalShiroFilterPlugin destroy.");
    }

    @Override
    public List<ExternalShiroFilter> getObject() {
        return Collections.unmodifiableList(this.filters);
    }
}
