package com.btw.snaptao.util;


/**
 * Created by thp on 2015/12/17.
 */
public interface HttpCallbackListener {
    /**
     * 网络监听接头
     */
    void onFinish(String response);

    void onError(Exception e);
}
