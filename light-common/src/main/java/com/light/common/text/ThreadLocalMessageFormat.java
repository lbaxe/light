package com.light.common.text;

import java.text.MessageFormat;

public class ThreadLocalMessageFormat extends ThreadLocal<MessageFormat> {
    MessageFormat messageFormat;

    public ThreadLocalMessageFormat(String pattern) {
        MessageFormat tmp = new MessageFormat(pattern);
        this.messageFormat = tmp;
    }

    @Override
    protected MessageFormat initialValue() {
        return (MessageFormat)this.messageFormat.clone();
    }
}