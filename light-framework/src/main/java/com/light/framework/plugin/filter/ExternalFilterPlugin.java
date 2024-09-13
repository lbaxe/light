package com.light.framework.plugin.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.light.framework.mvc.filter.external.ExternalFilter;
import com.light.framework.plugin.IPlugin;

public class ExternalFilterPlugin implements IPlugin<List<ExternalFilter>> {
    private static final Logger logger = LoggerFactory.getLogger(ExternalFilterPlugin.class);

    private final List<ExternalFilter> filters = new ArrayList<>();

    private ExternalFilterClassScan externalFilterClassScan;

    public ExternalFilterPlugin() {
        this.externalFilterClassScan = new ExternalFilterClassScan();
    }

    @Override
    public void init() {
        StringBuilder log = new StringBuilder();
        List<Class<?>> list = this.externalFilterClassScan.scan();
        for (Class<?> clazz : list) {
            try {
                this.filters.add((ExternalFilter)clazz.newInstance());
            } catch (Exception e) {
                logger.error("external filter load fail", e);
            }
            log.append("Added {" + clazz.getName()).append("} to requestInterceptor\n");
        }
        Collections.sort(this.filters, (o1, o2) -> {
            if (o1.getOrder() == o2.getOrder()) {
                return 0;
            }
            return (o1.getOrder() < o2.getOrder()) ? -1 : 1;
        });
        logger.info("ExternalFilterPlugin init.");
        logger.info(log.toString());
    }

    @Override
    public void destroy() {
        this.filters.clear();
        logger.info("ExternalFilterPlugin destroy.");
    }

    @Override
    public List<ExternalFilter> getObject() {
        return Collections.unmodifiableList(this.filters);
    }
}
