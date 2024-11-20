package com.light.framework.mvc.filter.light.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.light.framework.mvc.consts.WebConst;

/**
 * 默认处理静态资源
 */
public class StaticFilterHandler implements FilterHandler {
    private Set<String> excludeSuffixs;

    public StaticFilterHandler(String excludeStr) {
        if (excludeStr == null) {
            excludeStr = WebConst.FILTER_SUFFIX_EXCLUDE;
        }
        this.excludeSuffixs = Arrays.stream(excludeStr.split(",")).collect(Collectors.toSet());
    }

    @Override
    public boolean supports(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        return isStaticResource(servletPath, this.excludeSuffixs);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }

    /**
     * 判断是否请求静态资源
     *
     * @param servletPath
     * @return
     */
    public static boolean isStaticResource(String servletPath, final Set<String> inculdes) {
        int pos = servletPath.indexOf("?");
        String tmp = servletPath;
        if (pos != -1) {
            tmp = servletPath.substring(0, pos);
        }
        pos = tmp.lastIndexOf(".");
        if (pos == -1) {
            return false;
        }
        tmp = tmp.substring(pos);
        return inculdes.contains(tmp);
    }
}