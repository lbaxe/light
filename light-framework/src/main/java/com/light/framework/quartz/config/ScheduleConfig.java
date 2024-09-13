package com.light.framework.quartz.config;

import java.util.Properties;

import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * 定时任务配置（单机部署建议默认走内存，如需集群需要创建qrtz数据库表/打开类注释）
 * 
 */
@Configuration
public class ScheduleConfig {
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        // factory.setDataSource(dataSource);
        // quartz参数
        Properties prop = new Properties();
        prop.put("org.quartz.scheduler.instanceName", "DefaultQuartzScheduler");
        prop.put("org.quartz.scheduler.instanceId", "AUTO");
        // 线程池配置
        prop.put("org.quartz.threadPool.class", SimpleThreadPool.class.getCanonicalName());
        // 线程数 = CPU 核心数 /(1 - 阻塞系数)
        int threadCount = (int)(Runtime.getRuntime().availableProcessors() / (1 - 0.8));
        prop.put("org.quartz.threadPool.threadCount", threadCount + "");
        prop.put("org.quartz.threadPool.threadPriority", "5");
        // JobStore配置
        prop.put("org.quartz.jobStore.class", RAMJobStore.class.getCanonicalName());
        // 集群配置
        // prop.put("org.quartz.jobStore.isClustered", "true");
        // prop.put("org.quartz.jobStore.clusterCheckinInterval", "15000");
        // prop.put("org.quartz.jobStore.maxMisfiresToHandleAtATime", "1");
        // prop.put("org.quartz.jobStore.txIsolationLevelSerializable", "true");

        // sqlserver 启用
        // prop.put("org.quartz.jobStore.selectWithLockSQL", "SELECT * FROM {0}LOCKS UPDLOCK WHERE LOCK_NAME = ?");
        // prop.put("org.quartz.jobStore.misfireThreshold", "12000");
        // prop.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
        factory.setQuartzProperties(prop);

        factory.setSchedulerName("DefaultQuartzSchedulerFactory");
        // 延时启动
        factory.setStartupDelay(0);
        factory.setApplicationContextSchedulerContextKey("DefaultQuartzSchedulerApplicationContextSchedulerContextKey");
        // 可选，QuartzScheduler
        // 启动时更新己存在的Job，这样就不用每次修改targetObject后删除qrtz_job_details表对应记录了
        factory.setOverwriteExistingJobs(true);
        // 设置自动启动，默认为true
        factory.setAutoStartup(true);

        return factory;
    }
}