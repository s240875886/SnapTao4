package com.btw.snaptao.bean;

import org.json.JSONArray;

/**
 * Created by thp on 2015/12/18.
 * <p/>
 * 商品信息类
 */
public class StoreInfo {

    //商店的名字
    public String myname;
    //商店的地址
    public String myadd;
    //商店的电话
    public String mytel;
    //商品的名字
    public String item;
    //商店的图片链接
    public String img;
    //商店的价格
    public String price;
    //商店的折扣
    public String value;
    //商店的坐标
    public JSONArray coordinates;
    //获得经度
    public double lng;
    //获得纬度
    public double lat;
    //商店的LOGO图标链接
    public String iconUrl;

    public StoreInfo() {

    }

    public StoreInfo(String myname, String myadd, String mytel, String item, String img, String price, String value, JSONArray coordinates, double lng, double lat, String iconUrl) {
        this.myname = myname;
        this.myadd = myadd;
        this.mytel = mytel;
        this.item = item;
        this.img = img;
        this.price = price;
        this.value = value;
        this.coordinates = coordinates;
        this.lng = lng;
        this.lat = lat;
        this.iconUrl = iconUrl;
    }

    public String getMyname() {
        return myname;
    }

    public void setMyname(String myname) {
        this.myname = myname;
    }

    public String getMyadd() {
        return myadd;
    }

    public void setMyadd(String myadd) {
        this.myadd = myadd;
    }

    public String getMytel() {
        return mytel;
    }

    public void setMytel(String mytel) {
        this.mytel = mytel;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public JSONArray getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(JSONArray coordinates) {
        this.coordinates = coordinates;
    }

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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public String toString() {
        return "StoreInfo{" +
                "myname='" + myname + '\'' +
                ", myadd='" + myadd + '\'' +
                ", mytel='" + mytel + '\'' +
                ", item='" + item + '\'' +
                ", img='" + img + '\'' +
                ", price='" + price + '\'' +
                ", value='" + value + '\'' +
                ", coordinates=" + coordinates +
                ", lng=" + lng +
                ", lat=" + lat +
                ", iconUrl='" + iconUrl + '\'' +
                '}';
    }
}
