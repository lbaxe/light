package com.light.framework.quartz.task;

import com.light.framework.quartz.JobResponse;

public interface ITask {
    String taskKey();

    void start() throws Exception;

    void callback(JobResponse jobResponse);
}
