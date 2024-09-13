package com.light.framework.quartz.util;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.light.framework.quartz.ConcurrentQuartzJob;
import com.light.framework.quartz.JobMetaData;
import com.light.framework.quartz.ScheduleConst;
import com.light.framework.quartz.UnConcurrentQuartzJob;
import com.light.framework.util.ReflectUtil;

/**
 * 定时任务工具类
 */
public class ScheduleUtil {
    private static Logger logger = LoggerFactory.getLogger(ScheduleUtil.class);

    private static Class<? extends Job> getQuartzJobClass(int concurrent) {
        return concurrent == 0 ? UnConcurrentQuartzJob.class : ConcurrentQuartzJob.class;
    }

    /**
     * 创建定时任务
     */
    public static void registerQuartzJob(Scheduler scheduler, JobMetaData jobMetaData) throws SchedulerException {
        CronExpression cronExpression = null;
        try {
            cronExpression = new CronExpression(jobMetaData.getCron());
        } catch (ParseException e) {
            throw new RuntimeException("cron表达式解析异常");
        }
        Date startAt = CronUtil.getNextExecution(cronExpression);
        Date endAt = CronUtil.getNextExecution(cronExpression, startAt);
        if (startAt == null) {
            logger.warn("注册调度任务异常,jobKey=" + jobMetaData.getGroup() + "." + jobMetaData.getName() + ",cron表达式已过期");
            return;
        }
        // 构建job信息
        JobKey jobKey = JobKey.jobKey(jobMetaData.getName(), jobMetaData.getGroup());
        Class<? extends Job> jobClass = getQuartzJobClass(jobMetaData.getConcurrent());
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobKey).build();

        // 构建一个新的trigger
        TriggerKey triggerKey = TriggerKey.triggerKey(jobMetaData.getName(), jobMetaData.getGroup());
        Trigger trigger = null;
        if (jobMetaData.getJobType() == 0) {
            // 表达式调度构建器
            CronScheduleBuilder cronScheduleBuilder =
                CronScheduleBuilder.cronSchedule(jobMetaData.getCron()).withMisfireHandlingInstructionDoNothing();
            trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(cronScheduleBuilder)
                .startAt(startAt).build();
        } else {
            SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(24)
                .withRepeatCount(0).withMisfireHandlingInstructionIgnoreMisfires();
            trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(simpleScheduleBuilder)
                .startAt(startAt).endAt(endAt).build();
        }
        // 放入参数，运行时的方法可以获取
        jobDetail.getJobDataMap().put(ScheduleConst.JOB_METEDATA, jobMetaData);
        // 获取task初始参数
        Map<String, Object> jobInitParamMap = ReflectUtil.getGetterFieldValues(jobMetaData.getTask());
        jobDetail.getJobDataMap().put(ScheduleConst.JOB_INIT_PARAM, jobInitParamMap);
        // 判断是否存在
        if (scheduler.checkExists(jobKey)) {
            // 防止创建时存在数据问题 先移除，然后在执行创建操作
            scheduler.deleteJob(jobKey);
        }
        // 注册任务
        scheduler.scheduleJob(jobDetail, trigger);
        // 暂停任务
        scheduler.pauseJob(jobKey);
    }

    /**
     * 创建定时任务
     */
    public static void rescheduleQuartzJob(Scheduler scheduler, JobMetaData jobMetaData) throws SchedulerException {
        CronExpression cronExpression = null;
        try {
            cronExpression = new CronExpression(jobMetaData.getCron());
        } catch (ParseException e) {
            throw new RuntimeException("cron表达式解析异常");
        }
        Date startAt = CronUtil.getNextExecution(cronExpression);
        Date endAt = CronUtil.getNextExecution(cronExpression, startAt);
        if (startAt == null) {
            logger.warn("调度任务修改调度异常,jobKey=" + jobMetaData.getGroup() + "." + jobMetaData.getName() + ",cron表达式已过期");
            return;
        }

        // 构建一个新的trigger
        TriggerKey triggerKey = TriggerKey.triggerKey(jobMetaData.getName(), jobMetaData.getGroup());
        Trigger trigger = null;
        if (jobMetaData.getJobType() == 0) {
            // 表达式调度构建器
            CronScheduleBuilder cronScheduleBuilder =
                CronScheduleBuilder.cronSchedule(jobMetaData.getCron()).withMisfireHandlingInstructionDoNothing();
            trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(cronScheduleBuilder)
                .startAt(startAt).build();
        } else {
            SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(24)
                .withRepeatCount(0).withMisfireHandlingInstructionIgnoreMisfires();
            trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(simpleScheduleBuilder)
                .startAt(startAt).endAt(endAt).build();
        }

        scheduler.rescheduleJob(triggerKey, trigger);
    }
}