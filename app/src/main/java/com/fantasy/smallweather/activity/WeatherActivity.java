package com.fantasy.smallweather.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fantasy.smallweather.R;
import com.fantasy.smallweather.util.ActivityCollector;
import com.fantasy.smallweather.util.BaseActivity;
import com.fantasy.smallweather.util.HttpCallbackListener;
import com.fantasy.smallweather.util.HttpUtil;
import com.fantasy.smallweather.util.Utility;

/**
 * @author Fantasy
 * @version 1.0, 2016/8/29
 */
public class WeatherActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout weatherInfoLayout;
    private Button buttonSwitchArea;
    private Button buttonRefreshWeather;
    private TextView areaName;
    private TextView publishTime;
    private TextView currentData;
    private TextView weatherDesp;
    private TextView tmpMin;
    private TextView tmpMax;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    /** 点击返回键的时间 */
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);

        areaName = (TextView) findViewById(R.id.area_name);
        publishTime = (TextView) findViewById(R.id.publish_time);
        currentData = (TextView) findViewById(R.id.current_data);
        weatherDesp = (TextView) findViewById(R.id.weather_desp);
        tmpMin = (TextView) findViewById(R.id.tmp_min);
        tmpMax = (TextView) findViewById(R.id.tmp_max);
        buttonSwitchArea = (Button) findViewById(R.id.switch_area);
        buttonRefreshWeather = (Button) findViewById(R.id.refresh_weather);
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        buttonSwitchArea.setOnClickListener(this);
        buttonRefreshWeather.setOnClickListener(this);

        String areaCode = getIntent().getStringExtra("area_code");
        // 控件不可见，当控件visibility属性为INVISIBLE时，界面保留了view控件所占有的空间
        weatherInfoLayout.setVisibility(View.INVISIBLE);
        queryWeatherFromServer(areaCode);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_area:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                //finish();
                break;
            case R.id.refresh_weather:
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(this);
                String areaCode = prefs.getString("area_code", null);
                if (!TextUtils.isEmpty(areaCode)) {
                    queryWeatherFromServer(areaCode);
                } else {
                    Toast.makeText(this, "地区编号为空，无法更新天气信息",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    /**
     * 在服务器上查询所选地区的天气信息
     */
    private void queryWeatherFromServer(final String areaCode) {
        String address = "https://api.heweather.com/x3/weather?cityid=" +
                areaCode + "&key=" + ChooseAreaActivity.WEATHER_KEY;
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if (Utility.handleWeatherResponse(editor, response)) {
                    // 通过runOnUiThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            showWeather();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Toast.makeText(WeatherActivity.this,
                                    "成功连接服务器，但获取数据失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onError(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        e.printStackTrace();
                        closeProgressDialog();
                        Toast.makeText(WeatherActivity.this, "加载天气信息失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    /**
     * 从SharePreferences文件中读取存储的天气信息，并显示到界面上
     */
    private void showWeather() {
        areaName.setText(sharedPreferences.getString("area_name", null));
        publishTime.setText(sharedPreferences.getString("publish_time", null));
        currentData.setText(sharedPreferences.getString("current_date", null));
        tmpMin.setText(sharedPreferences.getString("tmp_min", null) + "℃");
        tmpMax.setText(sharedPreferences.getString("tmp_max", null) + "℃");

        if (sharedPreferences.getString("txt_d", null).equals
                (sharedPreferences.getString("txt_n", null))) {
            weatherDesp.setText(sharedPreferences.getString("txt_d", null));
        } else {
            weatherDesp.setText(sharedPreferences.getString("txt_d", null) + "转" +
                    sharedPreferences.getString("txt_n", null));
        }
        // 控件不可见，当控件visibility属性为INVISIBLE时，界面保留了view控件所占有的空间
        weatherInfoLayout.setVisibility(View.VISIBLE);
        //weatherInfoLayout.setVisibility(View.VISIBLE);
        //cityNameText.setVisibility(View.VISIBLE);
        //Intent intent = new Intent(this, AutoUpdateService.class);
        //startService(intent);
    }
    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
    /**
     * 实现点击两次返回键，退出程序的功能
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis() - exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                ActivityCollector.finishAll();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
