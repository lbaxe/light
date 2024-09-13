package com.light.framework.mvc.filter.handler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.light.framework.util.UrlUtil;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.light.core.conts.Const;
import com.light.core.exception.ServiceException;
import com.light.core.util.SpringContextUtil;
import com.light.core.util.ThreadLocalUtil;
import com.light.core.util.TraceIdUtil;
import com.light.framework.mvc.filter.GenericFilterBean;
import com.light.framework.mvc.filter.ProxyFilterChain;
import com.light.framework.mvc.util.RequestUtil;

/**
 * 默认处理ajax请求
 */
public class AjaxFilterHandler implements FilterHandler {
    private List<GenericFilterBean> filters;

    public AjaxFilterHandler(List<GenericFilterBean> filters) {
        this.filters = filters;
    }

    @Override
    public boolean supports(HttpServletRequest request) {
        RequestMappingHandlerMapping requestMappingHandlerMapping =
            SpringContextUtil.getBean(RequestMappingHandlerMapping.class);
        return !UrlUtil.isRPC(request.getServletPath())
            && (RequestUtil.isJsonRequest(requestMappingHandlerMapping, request) || RequestUtil.isAjaxRequest(request));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        ProxyFilterChain pfc = new ProxyFilterChain(filterChain, filters, preProcessor -> {
            Map<String, Object> inputMap = RequestUtil.getInput(request);
            if (inputMap == null) {
                throw new ServiceException("无效的请求");
            }
            inputMap.put(Const.TRACE_ID, TraceIdUtil.createTraceId().toString());
            String currentUserId = ThreadLocalUtil.getInstance().getCurrentUserId();
            inputMap.put(Const.CURRENT_USER_ID,
                StringUtils.isBlank(currentUserId) ? 0 : Integer.parseInt(currentUserId));
        });
        pfc.doFilter(request, response);
    }
}