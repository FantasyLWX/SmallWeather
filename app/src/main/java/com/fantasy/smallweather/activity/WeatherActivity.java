package com.fantasy.smallweather.activity;

import android.app.Activity;
import android.os.Bundle;

import com.fantasy.smallweather.R;

/**
 * @author Fantasy
 * @version 1.0, 2016/8/29.
 */
public class WeatherActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
    }
}
