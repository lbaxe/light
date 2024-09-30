package com.light.core.util;

public final class TraceId {
    private String uniqueId;

    private int serialNum;

    public TraceId() {
        this.uniqueId = "td." + System.currentTimeMillis();
    }

    public TraceId(String traceId) {
        this.uniqueId = traceId;
    }

    public void iterate() {
        this.serialNum++;
    }

    @Override
    public String toString() {
        if (this.serialNum == 0) {
            return this.uniqueId;
        }
        return this.uniqueId + "." + this.serialNum;
    }
}
