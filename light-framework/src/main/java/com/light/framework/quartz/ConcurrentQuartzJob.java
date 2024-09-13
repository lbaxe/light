package com.light.framework.quartz;

/**
 * <ul>
 * <li>每次调度都会生成新的job实例</li>
 * <li>若上一次任务未执行完成,到下一个周期时间点仍会执行</li>
 * <li>quartz每次调度都会重新生成任务的实例，不会出现成员变量的并发问题</li>
 * </ul>
 */
public class ConcurrentQuartzJob extends AbstractQuartzJob {

}
