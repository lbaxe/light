package com.light.framework.quartz.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.utils.DateUtils;
import org.quartz.CronExpression;
import org.quartz.TriggerUtils;
import org.quartz.impl.triggers.CronTriggerImpl;

/**
 * cron表达式工具类
 *
 */
public class CronUtil {
    /**
     * 返回一个布尔值代表一个给定的Cron表达式的有效性
     *
     * @param cronExpression Cron表达式
     * @return boolean 表达式是否有效
     */
    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * 返回一个字符串值,表示该消息无效Cron表达式给出有效性
     *
     * @param cronExpression Cron表达式
     * @return String 无效时返回表达式错误描述,如果有效返回null
     */
    public static String getInvalidMessage(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return null;
        } catch (ParseException pe) {
            return pe.getMessage();
        }
    }

    /**
     * 返回下一个执行时间根据给定的Cron表达式
     *
     * @param cron Cron表达式
     * @return Date 下次Cron表达式执行时间
     */
    public static Date getNextExecution(String cron) {
        try {
            CronExpression cronExpression = new CronExpression(cron);
            return getNextExecution(cronExpression, new Date(System.currentTimeMillis()));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * 返回下一个执行时间根据给定的Cron表达式
     *
     * @param cron Cron表达式
     * @return Date 下次Cron表达式执行时间
     */
    public static Date getNextExecution(CronExpression cronExpression) {
        return getNextExecution(cronExpression, new Date(System.currentTimeMillis()));
    }

    /**
     * 返回指定时间之后的下一个执行时间根据给定的Cron表达式
     * 
     * @param cronExpression
     * @param date
     * @return
     */
    public static Date getNextExecution(CronExpression cronExpression, Date date) {
        return cronExpression.getNextValidTimeAfter(date);
    }
    /**
     * 通过表达式获取近10次的执行时间
     * 
     * @param cron 表达式
     * @return 时间列表
     */
    public static List<String> getRecentTriggerTime(String cron) {
        List<String> list = new ArrayList<String>();
        try {
            CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
            cronTriggerImpl.setCronExpression(cron);
            List<Date> dates = TriggerUtils.computeFireTimes(cronTriggerImpl, null, 10);
            for (Date date : dates) {
                list.add(DateUtils.formatDate(date, "yyyy-MM-dd HH:mm:ss"));
            }
        } catch (ParseException e) {
            return null;
        }
        return list;
    }
}
