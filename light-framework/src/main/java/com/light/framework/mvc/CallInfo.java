package com.light.framework.mvc;

public class CallInfo {
    private String status;

    private long time;

    private String servletPath;

    private String clientIP;

    private long start;

    private String input = "{}";

    private String output = "{}";

    public long getTime() {
        return this.time;
    }

    public void begin() {
        this.start = System.currentTimeMillis();
    }

    public void end() {
        this.time = System.currentTimeMillis() - this.start;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public String getClientIP() {
        return this.clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInput() {
        return this.input;
    }

    public void setInput(String input) {
        if (input != null) {
            this.input = input;
        }
    }

    public String getOutput() {
        return this.output;
    }

    public void setOutput(String output) {
        if (output != null)
            this.output = output;
    }

    @Override
    public String toString() {
        return "CallInfo [status='" + status + "',time=" + time + ",servletPath='" + servletPath + "',clientIP='"
            + clientIP + "',start=" + start + ",input='" + input + "',output='" + output + "']";
    }
}
