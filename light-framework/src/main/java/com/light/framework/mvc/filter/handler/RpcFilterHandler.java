package com.light.framework.mvc.filter.handler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.light.core.conts.Const;
import com.light.core.exception.ServiceException;
import com.light.core.util.ThreadLocalUtil;
import com.light.core.util.TraceIdUtil;
import com.light.framework.mvc.filter.GenericFilterBean;
import com.light.framework.mvc.filter.ProxyFilterChain;
import com.light.framework.mvc.util.RequestUtil;
import com.light.framework.util.UrlUtil;

/**
 * 默认处理rpc请求
 */
public class RpcFilterHandler implements FilterHandler {
    private List<GenericFilterBean> filters;

    public RpcFilterHandler(List<GenericFilterBean> filters) {
        this.filters = filters;
    }

    @Override
    public boolean supports(HttpServletRequest request) {
        return UrlUtil.isRPC(request.getServletPath());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        ProxyFilterChain pfc = new ProxyFilterChain(filterChain, filters, preProcessor -> {
            Map<String, Object> inputMap = RequestUtil.getInput(request);
            if (inputMap == null) {
                throw new ServiceException("无效的请求");
            }
            // 获取请求的客户端IP
            String ip = request.getHeader(Const.CLIENT_HEADER.X_CLIENT_IP);
            if (StringUtils.isNotBlank(ip)) {
                ThreadLocalUtil.getInstance().setClientIP(ip);
            }
            // 获取请求的客户端traceId
            String traceId = request.getHeader(Const.CLIENT_HEADER.X_CLIENT_TRACE_ID);
            TraceIdUtil.trace(traceId);
            inputMap.put(Const.TRACE_ID, TraceIdUtil.trace(TraceIdUtil.nextTraceId()).toString());
        });
        pfc.doFilter(request, response);
    }
}