package com.light.mapper.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SlowQueryLogger {
    private static final Logger logger = LoggerFactory.getLogger(SlowQueryLogger.class);

    static {
        logger.info(SqlStopWatch.template());
    }

    public static final void log(SqlStopWatch watch) {
        logger.info(watch.sqlLog());
    }
}
