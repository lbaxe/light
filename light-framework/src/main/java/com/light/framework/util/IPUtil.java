package com.light.framework.util;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class IPUtil {
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (ip != null && ip.trim().length() != 0) {
            return ip.trim();
        }
        ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.indexOf(',') > 0) {
            String[] tmp = ip.split("[,]");
            for (int i = 0; tmp != null && i < tmp.length; i++) {
                if (tmp[i] != null && tmp[i].length() > 0 && !"unknown".equalsIgnoreCase(tmp[i])) {
                    ip = tmp[i].trim();
                    break;
                }
            }
        }
        if (!isUnkown(ip) && !isInternalIp(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    public static String getIpAddr(Map<String, String> headers) {
        String ip = headers.get("X-Real-IP");
        if (ip != null && ip.trim().length() != 0)
            return ip.trim();
        ip = headers.get("X-Forwarded-For");
        if (ip != null && ip.indexOf(',') > 0) {
            String[] tmp = ip.split("[,]");
            for (int i = 0; tmp != null && i < tmp.length; i++) {
                if (tmp[i] != null && tmp[i].length() > 0 && !"unknown".equalsIgnoreCase(tmp[i])) {
                    ip = tmp[i].trim();
                    break;
                }
            }
        }
        if (!isUnkown(ip) && !isInternalIp(ip)) {
            return ip;
        }
        return null;
    }

    private static boolean isUnkown(String ip) {
        if (ip == null || ip.trim().length() == 0 || "unknown".equalsIgnoreCase(ip.trim()))
            return true;
        return false;
    }

    private static final Pattern internalProxies = Pattern
            .compile("10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|192\\.168\\.\\d{1,3}\\.\\d{1,3}|"
                    + "169\\.254\\.\\d{1,3}\\.\\d{1,3}|127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|"
                    + "172\\.1[6-9]{1}\\.\\d{1,3}\\.\\d{1,3}|172\\.2[0-9]{1}\\.\\d{1,3}\\.\\d{1,3}|"
                    + "172\\.3[0-1]{1}\\.\\d{1,3}\\.\\d{1,3}|0:0:0:0:0:0:0:1|::1");

    public static boolean isInternalIp(String ip) {
        return internalProxies != null && internalProxies.matcher(ip).matches();
    }

    private static boolean contain(int[][] expect, int[] actual) {
        int[] start = expect[0];
        int[] end = expect[1];
        for (int i = 0; i < actual.length; i++) {
            if (actual[i] < start[i] || actual[i] > end[i])
                return false;
        }
        return true;
    }
}
