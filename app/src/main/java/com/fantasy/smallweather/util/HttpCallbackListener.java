package com.fantasy.smallweather.util;

/**
 * 回调服务器返回的结果
 * @author Fantasy
 * @version 1.0, 2016/8/25.
 */
public interface HttpCallbackListener {

    void onFinish(String response);

    void onError(Exception e);
}
