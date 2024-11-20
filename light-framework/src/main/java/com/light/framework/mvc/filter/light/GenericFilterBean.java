package com.light.framework.mvc.filter.light;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.filter.OncePerRequestFilter;

import com.light.framework.mvc.filter.light.external.DefaultOptionFilter;
import com.light.framework.mvc.filter.light.external.ExternalFilter;
import com.light.framework.mvc.filter.light.handler.*;
import com.light.framework.mvc.filter.light.inner.ClientIPFilter;
import com.light.framework.mvc.filter.light.inner.RequestParamFilter;
import com.light.framework.mvc.filter.light.inner.XssFilter;
import com.light.framework.plugin.PluginContext;
import com.light.framework.plugin.filter.ExternalFilterPlugin;

public abstract class GenericFilterBean extends OncePerRequestFilter {
    private List<GenericFilterBean> optionsFilterChains = new ArrayList<>();
    private List<GenericFilterBean> normalFilterChains = new ArrayList<>();
    private List<GenericFilterBean> rpcFilterChains = new ArrayList<>();

    private List<FilterHandler> filterHandlers = new ArrayList<>();
    private FilterHandler pageFilterHandler = null;

    protected abstract String urlPattern();

    @Override
    public void initFilterBean() {
        normalFilterChains.add(new XssFilter());
        normalFilterChains.add(new RequestParamFilter());
        normalFilterChains.add(new ClientIPFilter());

        rpcFilterChains.add(new XssFilter());
        rpcFilterChains.add(new RequestParamFilter());
        rpcFilterChains.add(new ClientIPFilter());

        ExternalFilterPlugin component = PluginContext.get().loadPlugin(ExternalFilterPlugin.class);
        List<ExternalFilter> externalFilters = component.getObject();
        for (GenericFilterBean externalFilter : externalFilters) {
            if (DefaultOptionFilter.class.isAssignableFrom(externalFilter.getClass())) {
                optionsFilterChains.add(externalFilter);
                continue;
            }
            normalFilterChains.add(externalFilter);
        }
        if (optionsFilterChains.isEmpty()) {
            optionsFilterChains.add(new DefaultOptionFilter());
        }
        filterHandlers.add(new StaticFilterHandler(null));
        filterHandlers.add(new InnerFilterHandler());
        filterHandlers.add(new OptionsFilterHandler(optionsFilterChains));
        filterHandlers.add(new AjaxFilterHandler(normalFilterChains));
        filterHandlers.add(new RpcFilterHandler(rpcFilterChains));

        this.pageFilterHandler = new PageFilterHandler(normalFilterChains);
    }

    protected FilterHandler getFilterHandler(HttpServletRequest request) throws ServletException {
        for (FilterHandler filterHandler : this.filterHandlers) {
            if (filterHandler.supports(request)) {
                return filterHandler;
            }
        }
        return this.pageFilterHandler;
    }
}
