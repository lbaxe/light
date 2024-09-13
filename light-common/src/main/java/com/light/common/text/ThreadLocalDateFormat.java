package com.light.common.text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ThreadLocalDateFormat extends ThreadLocal<DateFormat> {
    DateFormat proto;

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public ThreadLocalDateFormat() {
        this(DEFAULT_PATTERN, true);
    }

    public ThreadLocalDateFormat(boolean lenient) {
        this(DEFAULT_PATTERN, lenient);
    }

    public ThreadLocalDateFormat(String pattern) {
        this(pattern, true);
    }

    public ThreadLocalDateFormat(String pattern, boolean lenient) {
        SimpleDateFormat tmp = new SimpleDateFormat(pattern);
        this.proto = tmp;
        this.proto.setLenient(lenient);
    }

    @Override
    protected DateFormat initialValue() {
        return (DateFormat)this.proto.clone();
    }
}