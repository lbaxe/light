package com.light.common.lbs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 电子围栏计算
 */
public class GeofencingUtil {

    /**
     * 地球半径(米)
     */
    private static final double EARTH_RADIUS = 6378137.0;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 计算是否在圆内
     * 
     * @param radius 半径（单位/米）
     * @param p1 圆心坐标
     * @param p2 判断点坐标
     * @return: boolean true:在圆内,false:在圆外
     */
    public static boolean isInCircle(double radius, Point p1, Point p2) {
        double radLat1 = rad(p1.getLat());
        double radLat2 = rad(p2.getLat());
        double a = radLat1 - radLat2;
        double b = rad(p1.getLng()) - rad(p2.getLng());
        double s = 2 * Math.asin(Math
            .sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return !(s > radius);
    }

    /**
     * 是否在矩形区域内
     * 
     * @param lng 测试点经度
     * @param lat 测试点纬度
     * @param minLng 矩形四个点中最小经度
     * @param maxLng 矩形四个点中最大经度
     * @param minLat 矩形四个点中最小纬度
     * @param maxLat 矩形四个点中最大纬度
     * @return boolean true:在矩形内, false:在矩形外
     */
    public static boolean isInRectangleArea(double lng, double lat, double minLng, double maxLng, double minLat,
        double maxLat) {
        if (isInRange(lat, minLat, maxLat)) {// 如果在纬度的范围内
            if (minLng * maxLng > 0) {
                return isInRange(lng, minLng, maxLng);
            } else {
                if (Math.abs(minLng) + Math.abs(maxLng) < 180) {
                    return isInRange(lng, minLng, maxLng);
                } else {
                    double left = Math.max(minLng, maxLng);
                    double right = Math.min(minLng, maxLng);
                    return isInRange(lng, left, 180) || isInRange(lng, right, -180);
                }
            }
        } else {
            return false;
        }
    }

    /**
     * 是否在矩形区域内
     * 
     * @param point 测试点
     * @param gpsPoints 矩形GPS四个坐标点
     * @return boolean true:在矩形内, false:在矩形外
     */
    public static boolean isInRectangleArea(Point point, Point[] gpsPoints) {
        if (gpsPoints.length != 4) {
            return false;
        }
        double[] lats = new double[4];
        double[] lngs = new double[4];
        for (int i = 0; i < gpsPoints.length; i++) {
            lats[i] = gpsPoints[i].getLat();
            lngs[i] = gpsPoints[i].getLng();
        }
        Arrays.sort(lats);
        Arrays.sort(lngs);
        return isInRectangleArea(point.getLat(), point.getLng(), lats[0], lats[3], lngs[0], lngs[3]);
    }

    /**
     * 判断是否在经纬度范围内
     * 
     * @param point
     * @param left
     * @param right
     * @return boolean
     */
    public static boolean isInRange(double point, double left, double right) {
        return point >= Math.min(left, right) && point <= Math.max(left, right);
    }

    /**
     * 判断点是否在多边形内
     * 
     * @param point 测试点
     * @param pointList 多边形的点
     * @return boolean true:在多边形内, false:在多边形外
     * @throws
     */
    public static boolean isInPolygon(Point point, List<Point> pointList) {

        int N = pointList.size();
        boolean boundOrVertex = true;
        int intersectCount = 0;// 交叉点数量
        double precision = 2e-10; // 浮点类型计算时候与0比较时候的容差
        Point p1, p2;// 临近顶点
        Point p = point; // 当前点

        p1 = pointList.get(0);
        for (int i = 1; i <= N; ++i) {
            if (p.equals(p1)) {
                return boundOrVertex;
            }

            p2 = pointList.get(i % N);
            if (p.getLng() < Math.min(p1.getLng(), p2.getLng()) || p.getLng() > Math.max(p1.getLng(), p2.getLng())) {
                p1 = p2;
                continue;
            }

            // 射线穿过算法
            if (p.getLng() > Math.min(p1.getLng(), p2.getLng()) && p.getLng() < Math.max(p1.getLng(), p2.getLng())) {
                if (p.getLat() <= Math.max(p1.getLat(), p2.getLat())) {
                    if (p1.getLng() == p2.getLng() && p.getLat() >= Math.min(p1.getLat(), p2.getLat())) {
                        return boundOrVertex;
                    }

                    if (p1.getLat() == p2.getLat()) {
                        if (p1.getLat() == p.getLat()) {
                            return boundOrVertex;
                        } else {
                            ++intersectCount;
                        }
                    } else {
                        double xinters =
                            (p.getLng() - p1.getLng()) * (p2.getLat() - p1.getLat()) / (p2.getLng() - p1.getLng())
                                + p1.getLat();
                        if (Math.abs(p.getLat() - xinters) < precision) {
                            return boundOrVertex;
                        }

                        if (p.getLat() < xinters) {
                            ++intersectCount;
                        }
                    }
                }
            } else {
                if (p.getLng() == p2.getLng() && p.getLat() <= p2.getLat()) {
                    Point p3 = pointList.get((i + 1) % N);
                    if (p.getLng() >= Math.min(p1.getLng(), p3.getLng())
                        && p.getLng() <= Math.max(p1.getLng(), p3.getLng())) {
                        ++intersectCount;
                    } else {
                        intersectCount += 2;
                    }
                }
            }
            p1 = p2;
        }
        return intersectCount % 2 != 0;
    }

    public static void main(String[] args) {
        // 114.502868,38.050015
        // 114.503497,38.049715
        // 114.503402,38.049023
        // 114.502311,38.048909
        // 114.5021,38.049079
        // 114.501696,38.049523

        // 测试点 114.501826,38.049076 不在
        Point testP = new Point(114.502751, 38.050518);
        // 在
        Point testP1 = new Point(114.502387, 38.050781);

        Point p1 = new Point(114.502325, 38.050845);
        Point p2 = new Point(114.502787, 38.050816);
        Point p3 = new Point(114.502783, 38.050209);
        Point p4 = new Point(114.50228, 38.05022);
        Point p5 = new Point(114.50228, 38.050358);
        Point p6 = new Point(114.502473, 38.050358);
        Point p7 = new Point(114.502486, 38.050713);
        Point p8 = new Point(114.502311, 38.050717);
        List<Point> polygon = new ArrayList<>();
        polygon.add(p1);
        polygon.add(p2);
        polygon.add(p3);
        polygon.add(p4);
        polygon.add(p5);
        polygon.add(p6);
        polygon.add(p7);
        polygon.add(p8);

        boolean inside = GeofencingUtil.isInPolygon(testP, polygon);
        System.out.println("testP：" + inside);
        boolean inside1 = GeofencingUtil.isInPolygon(testP1, polygon);
        System.out.println("testP1：" + inside1);

    }
}