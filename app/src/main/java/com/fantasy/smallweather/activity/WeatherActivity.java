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
 * @version 1.1, 2016/09/02
 */
public class WeatherActivity extends BaseActivity implements View.OnClickListener {

    private Button buttonRefreshWeather;
    private TextView areaName;
    /** 当前温度 */
    private TextView nowTmp;
    /** 体感温度 */
    private TextView nowFl;
    /** 空气质量 */
    private TextView nowAqi;
    /** 空气湿度 */
    private TextView nowHum;
    /** 风向风力 */
    private TextView nowWind;
    private TextView data1;
    private TextView weatherDesp1;
    private TextView tmpRange1;
    private TextView data2;
    private TextView weatherDesp2;
    private TextView tmpRange2;
    private TextView data3;
    private TextView weatherDesp3;
    private TextView tmpRange3;
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
        nowTmp = (TextView) findViewById(R.id.now_tmp);
        nowFl = (TextView) findViewById(R.id.now_fl);
        nowAqi = (TextView) findViewById(R.id.now_aqi);
        nowHum = (TextView) findViewById(R.id.now_hum);
        nowWind = (TextView) findViewById(R.id.now_wind);
        data1 = (TextView) findViewById(R.id.data_1);
        weatherDesp1 = (TextView) findViewById(R.id.weather_desp_1);
        tmpRange1 = (TextView) findViewById(R.id.tmp_range1);
        data2 = (TextView) findViewById(R.id.data_2);
        weatherDesp2 = (TextView) findViewById(R.id.weather_desp_2);
        tmpRange2 = (TextView) findViewById(R.id.tmp_range2);
        data3 = (TextView) findViewById(R.id.data_3);
        weatherDesp3 = (TextView) findViewById(R.id.weather_desp_3);
        tmpRange3 = (TextView) findViewById(R.id.tmp_range3);

        buttonRefreshWeather = (Button) findViewById(R.id.refresh_weather);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        areaName.setOnClickListener(this);
        buttonRefreshWeather.setOnClickListener(this);

        String areaCode = getIntent().getStringExtra("area_code");
        queryWeatherFromServer(areaCode);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.area_name:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
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
                        Toast.makeText(WeatherActivity.this, "连接服务器失败",
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
        areaName.setText(sharedPreferences.getString("area_name", ""));
        nowTmp.setText(sharedPreferences.getString("now_tmp", ""));
        nowFl.setText(sharedPreferences.getString("now_fl", ""));
        nowAqi.setText(sharedPreferences.getString("now_aqi", ""));
        nowHum.setText(sharedPreferences.getString("now_hum", ""));
        nowWind.setText(sharedPreferences.getString("now_wind", ""));

        data1.setText(sharedPreferences.getString("date_1", ""));
        weatherDesp1.setText(sharedPreferences.getString("weather_desp_1", ""));
        tmpRange1.setText(sharedPreferences.getString("tmp_range_1", ""));

        data2.setText(sharedPreferences.getString("date_2", ""));
        weatherDesp2.setText(sharedPreferences.getString("weather_desp_2", ""));
        tmpRange2.setText(sharedPreferences.getString("tmp_range_2", ""));

        data3.setText(sharedPreferences.getString("date_3", ""));
        weatherDesp3.setText(sharedPreferences.getString("weather_desp_3", ""));
        tmpRange3.setText(sharedPreferences.getString("tmp_range_3", ""));
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
                Toast.makeText(getApplicationContext(), "再按一次返回键关闭程序",
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
