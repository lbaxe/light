//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.light.framework.mvc.filter.shiro;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;

import com.alibaba.fastjson2.JSON;
import com.light.core.exception.ServiceException;
import com.light.core.util.ThreadLocalUtil;
import com.light.framework.mvc.CallInfo;
import com.light.framework.mvc.log.CallLogger;
import com.light.framework.mvc.util.RequestUtil;
import com.light.framework.util.IPUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ExtendSpringShiroFilter extends AbstractShiroFilter {
    private CallLogger callLogger = new CallLogger();

    public ExtendSpringShiroFilter(WebSecurityManager webSecurityManager, FilterChainResolver resolver) {
        if (webSecurityManager == null) {
            throw new IllegalArgumentException("WebSecurityManager property cannot be null.");
        } else {
            this.setSecurityManager(webSecurityManager);
            if (resolver != null) {
                this.setFilterChainResolver(resolver);
            }

        }
    }

    @Override
    protected void doFilterInternal(ServletRequest servletRequest, ServletResponse servletResponse,
        final FilterChain chain) throws ServletException, IOException {

        Throwable t = null;

        final ServletRequest request = prepareServletRequest(servletRequest, servletResponse, chain);
        final ServletResponse response = prepareServletResponse(request, servletResponse, chain);
        final Subject subject = createSubject(request, response);

        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        HttpServletResponse httpServletResponse = (HttpServletResponse)response;

        CallInfo callInfo = new CallInfo();
        callInfo.setClientIP(IPUtil.getIpAddr(httpServletRequest));
        callInfo.setServletPath(httpServletRequest.getServletPath());
        callInfo.begin();
        try {
            subject.execute((Callable<Void>)() -> {
                updateSessionLastAccessTime(request, response);
                executeChain(request, response, chain);
                return null;
            });
        } catch (ExecutionException ex) {
            t = ex.getCause();
        } catch (Throwable throwable) {
            t = throwable;
        } finally {
            try {
                Map<String, Object> map = RequestUtil.getInput(httpServletRequest);
                if (map != null) {
                    Map<String, Object> tmp = new LinkedHashMap<>();
                    if (!map.containsKey("currentUserId")) {
                        String currentUserId = ThreadLocalUtil.getInstance().getCurrentUserId();
                        tmp.put("currentUserId",
                            StringUtils.isBlank(currentUserId) ? 0 : Integer.parseInt(currentUserId));
                    }
                    tmp.putAll(map);
                    map = tmp;
                }
                callInfo.setStatus(httpServletResponse.getStatus() + "");
                callInfo.setInput((map != null) ? JSON.toJSONString(map) : null);
                callInfo.setOutput(JSON.toJSONString(RequestUtil.getOutput(httpServletRequest)));
                callInfo.end();
                callLogger.log(callInfo);
            } finally {
                // 清除线程变量
                ThreadLocalUtil.getInstance().remove();
                // 清楚request属性
                RequestUtil.clean((HttpServletRequest)servletRequest);
            }
        }
        if (t != null) {
            if (t instanceof ServiceException) {
                throw (ServiceException)t;
            }
            if (t.getCause() instanceof ServiceException) {
                // log.error(t.getMessage(), t);
                throw (ServiceException)t.getCause();
            }
            // log.error(t.getMessage(), t);
            throw new ServiceException("系统异常，请稍后重试", t);
        }
    }
}
