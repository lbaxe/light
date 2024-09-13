package com.light.common.httpclient;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopWatch {
    private static final Logger logger = LoggerFactory.getLogger(StopWatch.class);

    private String name;

    private List<Long> timerList = new ArrayList<>();

    private boolean isDebug = false;

    public StopWatch(String name) {
        this(name, false);
    }

    public StopWatch(String name, boolean isDebug) {
        this.name = name;
        this.isDebug = isDebug;
    }

    public String getTimerLog() {
        StringBuilder sb = new StringBuilder(this.name + " TIME ");
        if (this.timerList.size() > 1) {
            for (int i = 0; i < this.timerList.size() - 1; i++) {
                long l0 = this.timerList.get(i);
                long l1 = this.timerList.get(i + 1);
                sb.append(l1 - l0);
                sb.append('/');
            }
            sb.append(this.timerList.get(this.timerList.size() - 1) - this.timerList.get(0));
        }
        sb.append(" ms");
        return sb.toString();
    }

    public void stop() {
        this.timerList.add(System.currentTimeMillis());
    }

    public void log() {
        if (this.isDebug) {
            logger.debug(getTimerLog());
        } else {
            logger.info(getTimerLog());
        }
    }

    public void log(long time) {
        StringBuilder sb = new StringBuilder(String.valueOf(this.name) + " TIME ");
        if (this.timerList.size() > 1) {
            for (int i = 0; i < this.timerList.size() - 1; i++) {
                long l0 = ((Long)this.timerList.get(i)).longValue();
                long l1 = ((Long)this.timerList.get(i + 1)).longValue();
                sb.append(l1 - l0);
                sb.append('/');
            }
            sb.append(time);
            sb.append('/');
            sb.append(((Long)this.timerList.get(this.timerList.size() - 1)).longValue()
                - ((Long)this.timerList.get(0)).longValue());
        }
        sb.append(" ms");
        if (this.isDebug) {
            logger.debug(sb.toString());
        } else {
            logger.info(sb.toString());
        }
    }
}
