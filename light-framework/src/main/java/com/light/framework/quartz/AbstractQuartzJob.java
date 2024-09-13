package com.light.framework.quartz;

import java.util.Map;
import java.util.UUID;

import com.light.framework.quartz.task.ITask;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.light.framework.util.ReflectUtil;

/**
 * 抽象quartz调用
 *
 */
public abstract class AbstractQuartzJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(AbstractQuartzJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobMetaData jobMetaData = (JobMetaData)context.getMergedJobDataMap().get(ScheduleConst.JOB_METEDATA);
        JobResponse response = new JobResponse();
        response.setName(jobMetaData.getName());
        response.setGroup(jobMetaData.getGroup());
        response.setFlowId(UUID.randomUUID().toString());
        response.setStartMillis(System.currentTimeMillis());
        Throwable throwable = null;
        try {
            ITask task = jobMetaData.getTask();
            // 获取task初始参数
            Map<String, Object> jobInitParamMap =
                (Map<String, Object>)context.getMergedJobDataMap().get(ScheduleConst.JOB_INIT_PARAM);
            ReflectUtil.setSetterFieldValues(task, jobInitParamMap);

            ReflectUtil.setSetterFieldValues(task, jobMetaData.getJobParam());
            jobMetaData.getTask().start();
        } catch (Exception e) {
            throwable = e;
            response.setException(e.getMessage());
            logger.error("任务调度,jobKey=" + jobMetaData.getGroup() + "." + jobMetaData.getName() + ",flowId="
                + response.getFlowId() + ", execute fail",
                e);
        } finally {
            // 记录结束时间
            response.setEndMillis(System.currentTimeMillis());
            JobDataMap jobDataMap = context.getMergedJobDataMap();
            jobDataMap.put(ScheduleConst.JOB_RESPONE, response);
            logger.info("任务调度,jobKey=" + jobMetaData.getGroup() + "." + jobMetaData.getName() + ",taskId="
                + response.getFlowId() + ", finish," + ((throwable == null) ? "no exception" : "has exception")
                + ", used: " + (response.getEndMillis() - response.getEndMillis()) + " ms");

            jobMetaData.getTask().callback(response);
        }
    }
}
