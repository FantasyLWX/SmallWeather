package com.fantasy.smallweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;

import com.fantasy.smallweather.R;
import com.fantasy.smallweather.util.BaseActivity;

/**
 * 欢迎界面
 * @author Fantasy
 * @version 1.2, 2016/09/03
 */
public class LaunchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_layout);

        // Handler中的postDelayed方法将一个Runnable对象加入主线程中执行，时间延迟3000ms后执行。
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences
                        (LaunchActivity.this);
                if (prefs.getString("area_code", null) == null) {
                    // 安装好APP后，第一次打开“小天气”或还未曾选择地区，则跳转到“选择地区界面”
                    Intent intent = new
                            Intent(LaunchActivity.this, ChooseAreaActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // 已经选择过地区了，则直接跳转到“天气信息界面”
                    Intent intent = new
                            Intent(LaunchActivity.this, WeatherActivity.class);
                    intent.putExtra("area_code", prefs.getString("area_code", null));
                    startActivity(intent);
                    finish();
                }
            }
        }, 3000);
    }

    // 屏蔽返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return false;
        }
        return false;
    }
}
