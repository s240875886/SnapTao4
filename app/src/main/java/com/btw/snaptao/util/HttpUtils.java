package com.btw.snaptao.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by thp on 2015/12/17.
 * <p/>
 * 网络工具类
 */

public class HttpUtils {
    public static String TAG = "HttpUtils";
    public static String GET_SUCCESS = "获取数据成功";
    public static String GET_FAILE = "获取数据失败";

    /**
     * Get请求
     *
     * @param address 请求的地址
     */
    public static String sendHttpRequest(final String address, HttpCallbackListener listener) {
        HttpURLConnection connection = null;
        try {
            //创建URL对象
            URL url = new URL(address);
            //打开网络连接
            connection = (HttpURLConnection) url.openConnection();
            //设置请求的方式
            connection.setRequestMethod("GET");
            //设置超时的时间
            connection.setReadTimeout(5000);
            //设置连接超时的时间
            connection.setConnectTimeout(5000);
            //获取响应码的状态
            int responecode = connection.getResponseCode();
            if (responecode == 200) {
                // 获取响应的输入流对象
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                if (listener != null) {
                    listener.onFinish(response.toString());
                }
            }
            return GET_SUCCESS;
        } catch (Exception e) {
            if (listener != null) {
                listener.onFinish(e.toString());
            }
            return GET_FAILE;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    /**
     * 下载图片 返回图片Bitmap
     *
     * @param urlString 图片的链接
     * @return Bitmap
     */
    public static Bitmap getNetWorkBitmap(String urlString) {

        URL imgUrl = null;
        Bitmap bitmap = null;
        HttpURLConnection urlConn = null;
        try {
            imgUrl = new URL(urlString);
            // 使用HttpURLConnection打开连接
            urlConn = (HttpURLConnection) imgUrl
                    .openConnection();
            urlConn.setConnectTimeout(5000);
            urlConn.setDoInput(true);
            urlConn.connect();

            // 将得到的数据转化成InputStream
            InputStream is = urlConn.getInputStream();
            // 将InputStream转换成Bitmap
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "网络错误或者请求失败：" + e.toString());
        } finally {
            urlConn.disconnect();
        }
        return bitmap;
    }

}
