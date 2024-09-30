package com.light.core.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.light.common.text.TextUtil;
import com.light.core.conts.Const;
import com.light.core.util.TraceIdUtil;

public class DebugLogger {
    private static final Logger logger = LoggerFactory.getLogger(DebugLogger.class);

    private static final DebugLogger debugLogger = new DebugLogger();

    public static DebugLogger getInstance() {
        return debugLogger;
    }

    public void log(String message) {
        this._log(message, null);
    }

    public void log(String message, Throwable t) {
        this._log(message, t);
    }

    private void _log(String message, Throwable t) {
        logger.info(Const.TRACE_ID + "=" + TraceIdUtil.getTraceId() + " ## "
            + ((message != null && message.trim().length() != 0) ? filter(message) : message), t);
    }

    public static String filter(String message) {
        return TextUtil.desensitize(message);
    }
}
