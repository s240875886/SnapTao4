package com.btw.snaptao.util;

import android.util.Log;

import com.btw.snaptao.bean.StoreInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by thp on 2015/12/18.
 */
public class JsonUtils {
    public static String TAG = "JsonUtils";

    public static ArrayList<StoreInfo> infos = null;

    /**
     * 解析Json 并返回StoreInfos商品信息类
     *
     * @param response
     * @return
     */
    public static ArrayList<StoreInfo> getStoreInfo(String response) {
        infos = new ArrayList<StoreInfo>();


        try {
            JSONArray feature = new JSONArray(response);
            //循环读取多条商品信息
            for (int i = 0; i < feature.length(); i++) {
                StoreInfo info = new StoreInfo();
                JSONObject features = feature.getJSONObject(i);
                JSONObject properties = features.getJSONObject("properties");

                JSONObject geometry = features.getJSONObject("geometry");
                /**
                 * 获得商店的 图标LOgo链接，商店图片链接，商店名字，商店地址，商品的名字，商店价格，商店折扣，商店坐标点
                 */
                String icon = properties.getJSONObject("icon").getString("iconUrl");
                String img = properties.getString("img");
                String title = properties.getString("title");
                String description = properties.getString("description");
                String item = properties.getString("item");
                String price = properties.getString("price");
                String value = properties.getString("value");
                JSONArray coordinates = geometry.getJSONArray("coordinates");
                double lng = coordinates.getDouble(0);
                double lat = coordinates.getDouble(1);
                //添加信息到商品类中
                info.setIconUrl(icon);
                info.setImg(img);
                info.setMyname(title);
                info.setMyadd(description);
                info.setItem(item);
                info.setPrice(price);
                info.setValue(value);
                info.setCoordinates(coordinates);
                info.setLng(lng);
                info.setLat(lat);
                infos.add(info);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Json解析失败：" + e.toString());
        }
        return infos;

    }
}
