package com.light.framework.mvc.filter.light.external;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.light.core.exception.ServiceException;

public class DefaultOptionFilter extends ExternalFilter {
    @Override
    public boolean preDoFilter(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        if ("OPTIONS".equals(request.getMethod())) {
            String origin = request.getHeader("Origin");
            if (origin != null) {
                if (!filter(origin)) {
                    response.setStatus(405);
                } else {
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                    response.setHeader("Access-Control-Allow-Origin", origin);
                    response.setHeader("Access-Control-Allow-Headers", "X-Requested-With,Authorization,Origin");
                    response.setHeader("Access-Control-Max-Age", "3600");
                }
            }
        }
        return false;
    }

    protected boolean filter(String origin) {
        return true;
    }
}