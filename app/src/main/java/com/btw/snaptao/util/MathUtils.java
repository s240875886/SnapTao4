package com.btw.snaptao.util;


import com.mapbox.mapboxsdk.constants.GeoConstants;
import com.mapbox.mapboxsdk.constants.MathConstants;


/**
 * Created by thp on 2015/12/14.
 */
public class MathUtils {
    public final static String TAG = "MathUtils";

    private static double rad(double d) {
        return d * MathConstants.PI / 180.0;
    }

    /**
     * 计算两坐标点之间的距离
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return 返回两坐标点之间的距离
     */
    public static double getDistancePoints(double lat1, double lng1,
                                           double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * GeoConstants.RADIUS_EARTH_METERS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }


}
