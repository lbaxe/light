package com.light.framework.mvc.filter.light;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSON;
import com.light.core.exception.ServiceException;
import com.light.core.util.ThreadLocalUtil;
import com.light.core.util.ThrowableUtil;
import com.light.framework.mvc.CallInfo;
import com.light.framework.mvc.filter.light.handler.FilterHandler;
import com.light.framework.mvc.filter.light.handler.InnerFilterHandler;
import com.light.framework.mvc.filter.light.handler.OptionsFilterHandler;
import com.light.framework.mvc.filter.light.handler.StaticFilterHandler;
import com.light.framework.mvc.log.CallLogger;
import com.light.framework.mvc.util.RequestUtil;

public class LightFilter extends GenericFilterBean {
    private Logger logger = LoggerFactory.getLogger(LightFilter.class);
    private CallLogger callLogger = new CallLogger();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        FilterHandler filterHandler = getFilterHandler(request);
        // 静态资源、预检请求、内部请求
        if (filterHandler instanceof StaticFilterHandler || filterHandler instanceof OptionsFilterHandler
            || filterHandler instanceof InnerFilterHandler) {
            filterHandler.handle(request, response, filterChain);
            return;
        }
        CallInfo callInfo = new CallInfo();
        callInfo.setServletPath(request.getServletPath());
        callInfo.setClientIP(ThreadLocalUtil.getInstance().getClientIP());
        callInfo.begin();
        try {
            filterHandler.handle(request, response, filterChain);
        } catch (Throwable t) {
            Throwable actual = ThrowableUtil.unwrapThrowable(t);
            if (actual instanceof ServiceException) {
                throw (ServiceException)t;
            }
            throw t;
        } finally {
            try {
                Map<String, Object> map = RequestUtil.getInput(request);
                Object output = RequestUtil.getOutput(request);
                // 执行handle后再次获取，rpc请求会根据请求头在doFilter前更新clientIP
                callInfo.setClientIP(ThreadLocalUtil.getInstance().getClientIP());
                callInfo.setStatus(response.getStatus() + "");

                callInfo.setInput(map != null ? JSON.toJSONString(map) : null);
                callInfo.setOutput(output != null ? JSON.toJSONString(output) : null);
                callInfo.end();
                callLogger.log(callInfo);
            } finally {
                ThreadLocalUtil.getInstance().remove();
                RequestUtil.clean(request);
            }
        }
    }

    @Override
    protected String urlPattern() {
        return "/*";
    }
}
