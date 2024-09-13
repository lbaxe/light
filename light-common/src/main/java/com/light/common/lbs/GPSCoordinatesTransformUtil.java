package com.light.common.lbs;

import java.util.HashMap;
import java.util.Map;

/**
 * 坐标转换工具类 天地图使用的是地球坐标系(WGS84); 高德地图使用的是火星坐标系(GCJ02); 百度地图使用的是百度坐标(bd09II)
 */
public class GPSCoordinatesTransformUtil {

    private static final double X_PI = 3.14159265358979324 * 3000.0 / 180.0;
    private static final double PI = 3.14159265358979324;
    // 卫星椭球坐标投影到平面地图坐标系的投影因子。 地球长半径
    private static final double EARTH_MAJOR_RADIUS = 6378245.0;
    // 椭球的偏心率。
    private static final double ECCENTRICITY_RATIO = 0.00669342162296594323;

    /**
     * @Discription: 百度坐标(bd09II)转火星坐标(GCJ02)
     * @Param lat 百度坐标纬度
     * @Param lon 百度坐标经度
     * @Created on: 2020/12/23 15:40
     * @author muyuanpei
     */
    public static Map<String, Double> baiduTomars(double lat, double lon) {
        Map<String, Double> mars_point = new HashMap<>();
        mars_point.put("lon", 0D);
        mars_point.put("lat", 0D);

        double x = lon - 0.0065;
        double y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        mars_point.put("lon", z * Math.cos(theta));
        mars_point.put("lat", z * Math.sin(theta));
        return mars_point;
    }

    /**
     * @Discription: 火星坐标系(GCJ02)转百度坐标系(bd09II)
     * @Param gcjLat 火星坐标纬度
     * @Param gcjLon 火星坐标经度
     * @Created on: 2020/12/23 15:42
     * @author muyuanpei
     */
    public static Map<String, Double> marsTobaidu(double gcjLat, double gcjLon) {
        HashMap<String, Double> baidu_point = new HashMap<>();
        baidu_point.put("lon", 0D);
        baidu_point.put("lat", 0D);

        double x = gcjLon;
        double y = gcjLat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * X_PI);
        baidu_point.put("lon", z * Math.cos(theta) + 0.0065);
        baidu_point.put("lat", z * Math.sin(theta) + 0.006);
        return baidu_point;
    }

    /**
     * @Discription: 火星坐标系(GCJ02)转地球坐标系(WGS84)
     * @Param lat 火星坐标纬度
     * @Param lon 火星坐标经度
     * @Created on: 2020/12/23 15:42
     * @author muyuanpei
     */
    public static Map<String, Double> transformGCJ2WGS(double gcjLat, double gcjLon) {
        Map<String, Double> map = delta(gcjLat, gcjLon);
        double lat = map.get("lat");
        double lon = map.get("lon");
        map.get("lon");
        map.put("lat", gcjLat - lat);
        map.put("lon", gcjLon - lon);
        return map;
    }

    private static Map delta(double lat, double lon) {
        double dLat = transformLat(lon - 105.0, lat - 35.0, PI);
        double dLon = transformLon(lon - 105.0, lat - 35.0, PI);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - ECCENTRICITY_RATIO * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((EARTH_MAJOR_RADIUS * (1 - ECCENTRICITY_RATIO)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (EARTH_MAJOR_RADIUS / sqrtMagic * Math.cos(radLat) * PI);

        Map<String, Double> map = new HashMap<>();
        map.put("lat", dLat);
        map.put("lon", dLon);
        return map;
    }

    /**
     * @Discription: 地球坐标系(WGS - 84)转火星坐标系(GCJ)
     * @Param wgLat 地球坐标纬度
     * @Param wgLon 地球坐标经度
     * @Created on: 2020/12/23 15:42
     * @author muyuanpei
     */
    public static Map<String, Double> transform(double wgLat, double wgLon) {

        HashMap<String, Double> mars_point = new HashMap<>();
        mars_point.put("lon", 0D);
        mars_point.put("lat", 0D);
        // 如果是国外坐标点，则直接返回传进来的坐标点
        if (outOfChina(wgLon, wgLat)) {
            mars_point.put("lon", wgLon);
            mars_point.put("lat", wgLat);
            return mars_point;
        }
        // 如果为国内坐标点，则计算偏移量
        double dLat = transformLat(wgLon - 105.0, wgLat - 35.0, PI);
        double dLon = transformLon(wgLon - 105.0, wgLat - 35.0, PI);
        double radLat = wgLat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - ECCENTRICITY_RATIO * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((EARTH_MAJOR_RADIUS * (1 - ECCENTRICITY_RATIO)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (EARTH_MAJOR_RADIUS / sqrtMagic * Math.cos(radLat) * PI);
        mars_point.put("lon", wgLon + dLon);
        mars_point.put("lat", wgLat + dLat);

        return mars_point;
    }

    // 公共方法
    /*判断是否在国内，不在国内则不做偏移*/
    private static boolean outOfChina(double lon, double lat) {
        if ((lon < 72.004 || lon > 137.8347) && (lat < 0.8293 || lat > 55.8271)) {
            return true;
        } else {
            return false;
        }
    }

    private static double transformLat(double x, double y, double pi) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLon(double x, double y, double pi) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }

}