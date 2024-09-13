package com.light.framework.quartz;

import java.util.Map;

import com.light.framework.quartz.task.ITask;

public class JobMetaData {
    private String name;
    private String group;
    private int concurrent;
    // 0周期性任务 1一次性任务
    private int jobType;
    private String cron;
    private Map<String, Object> jobParam;
    private ITask task;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(int concurrent) {
        this.concurrent = concurrent;
    }

    public int getJobType() {
        return jobType;
    }

    public void setJobType(int jobType) {
        this.jobType = jobType;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Map<String, Object> getJobParam() {
        return jobParam;
    }

    public void setJobParam(Map<String, Object> jobParam) {
        this.jobParam = jobParam;
    }

    public ITask getTask() {
        return task;
    }

    public void setTask(ITask task) {
        this.task = task;
    }
}
