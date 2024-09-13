package com.light.common.lbs;

public class Point {
    /**
     * 经度（-180~180，东经正数，西经负数）
     */
    private double lng;

    /**
     * 维度（-90~90，北纬正数，南纬负数）
     */
    private double lat;

    public Point(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public Point(String coordinate) {
        try {
            this.lng = Double.valueOf(coordinate.split(",")[0]);
            this.lat = Double.valueOf(coordinate.split(",")[1]);
        } catch (Exception e) {
            throw new RuntimeException("经纬度转换失败！");
        }
    }

    public Point() {}

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

}