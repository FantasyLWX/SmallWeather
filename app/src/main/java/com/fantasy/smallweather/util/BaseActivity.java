package com.fantasy.smallweather.util;

import android.app.Activity;
import android.os.Bundle;

/**
 * 自定义活动类，重载了onCreate()和onDestroy()
 * @author Fantasy
 * @version 1.0, 2016/8/31
 */
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
