package com.light.framework.mvc.log;

import org.slf4j.Logger;

import com.light.common.text.ThreadLocalMessageFormat;
import com.light.core.util.Log4j2Util;
import com.light.framework.mvc.CallInfo;

public class CallLogger {
    private static final Logger logger = Log4j2Util.register("call", "call");

    private static final String pattern =
        "status={0}, time={1}ms, servletPath={2}, clientIP={3}, input={4}, output={5}";

    private static ThreadLocalMessageFormat messageFormat = new ThreadLocalMessageFormat(pattern);

    public void log(CallInfo callInfo) {
        callInfo.setInput(omitChars(callInfo.getInput(), 512));
        callInfo.setOutput(omitChars(callInfo.getOutput(), 512));
        String info = messageFormat.get().format(getVars(callInfo));
        logger.info(info);
    }

    private Object[] getVars(CallInfo callInfo) {
        return new Object[] {callInfo.getStatus(), Long.valueOf(callInfo.getTime()), callInfo.getServletPath(),
            callInfo.getClientIP(), callInfo.getInput(), callInfo.getOutput()};
    }

    public static String omitChars(String chars, int max) {
        if (chars == null)
            return "";
        int omittedCharCnt = chars.length() - max;
        if (omittedCharCnt > 0) {
            String output = chars.substring(0, max);
            return output + "(... more " + omittedCharCnt + " chars omitted)";
        }
        return chars;
    }
}
