package com.light.core.util;

import java.nio.charset.Charset;

import com.light.core.conts.Const;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.light.common.conf.SystemUtil;

public class Log4j2Util {

    public static Logger register(String category, String filename) {
        // 传递false，会根据classloader地址类型决定是否新建context；传false，避免xml配置context未实例化，导致重新生成default的context，保证了与xml的context相同的classloader使用相同的上下文
        LoggerContext context = (LoggerContext)LogManager.getContext(false);
        Configuration configuration = context.getConfiguration();
        // 配置PatternLayout输出格式
        PatternLayout layout =
            PatternLayout.newBuilder().withConfiguration(configuration).withCharset(Charset.forName("utf-8"))
                .withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%t] %c | %msg%n").build();
        // 配置基于时间的滚动策略
        TimeBasedTriggeringPolicy timePolicy = TimeBasedTriggeringPolicy.newBuilder().withInterval(1).build();
        SizeBasedTriggeringPolicy sizePolicy = SizeBasedTriggeringPolicy.createPolicy(1024 * 1024 * 100 + "");
        CompositeTriggeringPolicy compositePolicy = CompositeTriggeringPolicy.createPolicy(timePolicy, sizePolicy);
        // 配置同类型日志策略
        DefaultRolloverStrategy strategy1 =
            DefaultRolloverStrategy.newBuilder().withConfig(configuration).withFileIndex("max").withMax("7").build();
        // 配置appender
        RollingFileAppender appender = RollingFileAppender.newBuilder().setConfiguration(configuration)
            .setName(category).setLayout(layout)
            .setFilter(ThresholdFilter.createFilter(Level.INFO, Filter.Result.ACCEPT, Filter.Result.DENY))
            .withFileName(getLogRootDirectory() + category + SystemUtil.FILE_SEPARATOR + filename + ".log")
            .withFilePattern("logs/" + category + SystemUtil.FILE_SEPARATOR + filename + "-%d{yyyy-MM-dd}_%i.log.gz")
            .withPolicy(compositePolicy).withStrategy(strategy1).withAppend(true).build();
        // 改变appender状态
        appender.start();
        configuration.addAppender(appender);
        // 新建logger
        String loggerName = Const.NAMESPACE_LOG4J_CATEGORY + category;
        LoggerConfig loggerConfig = new LoggerConfig(loggerName, Level.INFO, false);
        loggerConfig.addAppender(appender, Level.INFO,
            ThresholdFilter.createFilter(Level.INFO, Filter.Result.ACCEPT, Filter.Result.DENY));

        configuration.addLogger(loggerName, loggerConfig);
        context.updateLoggers(configuration);

        return LoggerFactory.getLogger(loggerName);
    }

    private static String getLogRootDirectory() {
        return SystemUtil.getDeployDirectory("logs");
    }
}