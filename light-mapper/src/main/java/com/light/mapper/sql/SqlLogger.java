package com.light.mapper.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SqlLogger {
    private static final Logger logger = LoggerFactory.getLogger(SqlLogger.class);

    static {
        logger.info(SqlStopWatch.template());
    }

    public static final void log(SqlStopWatch watch) {
        logger.info(watch.sqlLog());
    }
}
