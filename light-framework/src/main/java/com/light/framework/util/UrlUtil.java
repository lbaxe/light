package com.light.framework.util;

import com.light.core.conts.Const;

public class UrlUtil {
    public static final String getNamespace4API() {
        return "/" + Const.NAMESPACE_API + "/";
    }

    public static boolean isRPC(String servletPath) {
        return servletPath.startsWith(getNamespace4API());
    }
}
