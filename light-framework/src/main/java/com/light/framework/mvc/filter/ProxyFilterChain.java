package com.light.framework.mvc.filter;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyFilterChain implements FilterChain {

    private static final Logger log = LoggerFactory.getLogger(ProxyFilterChain.class);

    private FilterChain orig;
    private List<GenericFilterBean> filters;
    private int index = 0;
    Consumer<ServletRequest> preProcessorConsumer;

    public ProxyFilterChain(FilterChain orig, List<GenericFilterBean> filters) {
        this(orig, filters, null);
    }

    public ProxyFilterChain(FilterChain orig, List<GenericFilterBean> filters,
        Consumer<ServletRequest> preProcessorConsumer) {
        if (orig == null) {
            throw new NullPointerException("original FilterChain cannot be null.");
        }
        this.orig = orig;
        this.filters = filters;
        this.index = 0;
        this.preProcessorConsumer = preProcessorConsumer;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (this.filters == null || this.filters.size() == this.index) {
            // we've reached the end of the wrapped chain, so invoke the original one:
            if (log.isTraceEnabled()) {
                log.trace("Invoking original filter chain.");
            }
            if (preProcessorConsumer != null) {
                preProcessorConsumer.accept(request);
            }
            this.orig.doFilter(request, response);
        } else {
            if (log.isTraceEnabled()) {
                log.trace("Invoking wrapped filter at index [" + this.index + "]");
            }
            this.filters.get(this.index++).doFilter(request, response, this);
        }
    }
}
