package com.light.common.other;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.light.common.text.ThreadLocalDateFormat;

public class DateUtil {
    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);

    private static ThreadLocalDateFormat defalutDateTimeFmt = new ThreadLocalDateFormat();

    private static ThreadLocalDateFormat defalutDateFmt = new ThreadLocalDateFormat("yyyy-MM-dd");

    /**
     * 获取当天的剩余毫秒数
     * 
     * @return
     */
    public static long getThisDayOffMillis() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() - System.currentTimeMillis();
    }

    /**
     * 获取本周的剩余毫秒数
     *
     * @return
     */
    public static long getThisWeekOffMillis() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        if (dayWeek == 1) {// 周日直接加1
            cal.add(Calendar.DATE, 1);
        } else {
            // 2 3 4 5 6 7
            cal.add(Calendar.DATE, 7 - (dayWeek - cal.getFirstDayOfWeek()));
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis() - System.currentTimeMillis();
    }

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        return getCurrentDate(calendar);
    }

    public static String getCurrentDate(Calendar calendar) {
        return defalutDateFmt.get().format(calendar.getTime());
    }

    public static String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        return getCurrentDateTime(calendar);
    }

    public static String getCurrentDateTime(Calendar calendar) {
        return defalutDateTimeFmt.get().format(calendar.getTime());
    }

    public static String format(String yyyymmddhhmmss) {
        if (yyyymmddhhmmss == null) {
            return "";
        }
        StringBuffer ret = new StringBuffer();
        if (yyyymmddhhmmss != null) {
            if (yyyymmddhhmmss.length() == 8) {
                ret.append(yyyymmddhhmmss, 0, 4);
                ret.append("-");
                ret.append(yyyymmddhhmmss, 4, 6);
                ret.append("-");
                ret.append(yyyymmddhhmmss, 6, 8);
                return ret.toString();
            }
            if (yyyymmddhhmmss.length() >= 10) {
                ret.append(" ");
            }
            if (yyyymmddhhmmss.length() == 10) {
                ret.append(yyyymmddhhmmss, 8, 10);
                return ret.toString();
            }
            if (yyyymmddhhmmss.length() == 12) {
                ret.append(yyyymmddhhmmss, 8, 10);
                ret.append(":");
                ret.append(yyyymmddhhmmss, 10, 12);
            }
            if (yyyymmddhhmmss.length() == 14) {
                ret.append(yyyymmddhhmmss, 8, 10);
                ret.append(":");
                ret.append(yyyymmddhhmmss, 10, 12);
                ret.append(":");
                ret.append(yyyymmddhhmmss, 12, 14);
                return ret.toString();
            }
        }
        return yyyymmddhhmmss;
    }

    /**
     * 获取当年当月第一天
     * 
     * @return
     */
    public static String getMonthFirstDay() {
        return DateUtil.getMonthFirstDay(Calendar.getInstance().get(Calendar.MONTH) + 1);
    }

    /**
     * 获取指定月第一天
     * 
     * @param month
     * @return
     */
    public static String getMonthFirstDay(int month) {
        return DateUtil.getMonthFirstDay(Calendar.getInstance().get(Calendar.YEAR), month);
    }

    /**
     * 获取指定年月第一天
     * 
     * @param year
     * @param month 范围1-12
     * @return
     */
    public static String getMonthFirstDay(int year, int month) {
        if (month < 0 || month > 12) {
            throw new RuntimeException("非法月份");
        }
        // 获取当前年份、月份、日期
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return defalutDateFmt.get().format(calendar.getTime());
    }

    /**
     * 获取当年当月最后一天
     * 
     * @return
     */
    public static String getMonthLastDay() {
        return DateUtil.getMonthLastDay(Calendar.getInstance().get(Calendar.MONTH) + 1);
    }

    /**
     * 获取指定月最后一天
     * 
     * @param month 范围 1-12
     * @return
     */
    public static String getMonthLastDay(int month) {
        return DateUtil.getMonthLastDay(Calendar.getInstance().get(Calendar.YEAR), month);
    }

    /**
     * 获取指定年月最后一天
     * 
     * @param year
     * @param month 范围1-12
     * @return
     */
    public static String getMonthLastDay(int year, int month) {
        if (month < 0 || month > 12) {
            throw new RuntimeException("非法月份");
        }
        // 获取当前年份、月份、日期
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        return defalutDateFmt.get().format(calendar.getTime());
    }

    public static void main(String[] args) {
        System.out.println(DateUtil.getMonthFirstDay());
        System.out.println(DateUtil.getMonthLastDay());
    }
}
