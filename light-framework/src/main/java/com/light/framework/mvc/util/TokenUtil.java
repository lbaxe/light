package com.light.framework.mvc.util;

import javax.servlet.http.HttpServletRequest;

import com.light.framework.auth.consts.AuthConst;

public class TokenUtil {
    /**
     * 从请求获取token字符串
     *
     * @param request
     * @return
     */
    public static String getToken(HttpServletRequest request) {
        return request.getHeader(AuthConst.AUTH_KEY);
    }
}
