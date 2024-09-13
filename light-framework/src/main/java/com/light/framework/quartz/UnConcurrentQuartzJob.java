package com.light.framework.quartz;

import org.quartz.DisallowConcurrentExecution;

/**
 * <ul>
 * <li>每次调度都会生成新的job实例</li>
 * <li>DisallowConcurrentExecution 若上一次任务未执行完成,控制到下一个周期时间点不执行</li>
 * <li>quartz每次调度都会重新生成任务的实例，不会出现成员变量的并发问题</li>
 * </ul>
 */
@DisallowConcurrentExecution
public class UnConcurrentQuartzJob extends AbstractQuartzJob {

}
