package com.light.framework.plugin.schedule;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.light.framework.quartz.task.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.light.core.util.SpringContextUtil;
import com.light.framework.plugin.IPlugin;

public class TaskPlugin implements IPlugin<Map<String, ITask>> {
    private static final Logger logger = LoggerFactory.getLogger(TaskPlugin.class);

    private final Map<String, ITask> taskMap = new HashMap<>();
    private TaskClassScan taskClassScan;

    public TaskPlugin() {
        this.taskClassScan = new TaskClassScan();
    }

    @Override
    public void init() {
        List<Class<?>> taskClasses = this.taskClassScan.scan();
        for (Class clazz : taskClasses) {
            try {
                ITask task = (ITask)SpringContextUtil.getBean(clazz);
                this.taskMap.put(task.taskKey(), task);
            } catch (Exception e) {
                logger.error("task load fail", e);
            }
        }
        logger.info("TaskPlugin init.");
    }

    @Override
    public void destroy() {
        taskMap.clear();
        logger.info("TaskPlugin destroy");
    }

    @Override
    public Map<String, ITask> getObject() {
        return Collections.unmodifiableMap(this.taskMap);
    }
}
