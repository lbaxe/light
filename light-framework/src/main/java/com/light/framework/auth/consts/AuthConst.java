package com.light.framework.auth.consts;

import java.util.concurrent.TimeUnit;

public final class AuthConst {
    public static final String AUTH_ISSUER = "lbaxe";
    public static final String AUTH_KEY = "Authorization";
    public static final String AUTH_VERSION = "1.0";
    public static final int AUTH_TYPE_PC = 0;
    public static final long AUTH_TIME_OUT_DAY = 1L;
    public static final long ONE_DAY_MILLISECONDS = TimeUnit.DAYS.toMillis(1L);
}
