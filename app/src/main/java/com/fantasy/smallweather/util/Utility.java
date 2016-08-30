package com.fantasy.smallweather.util;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.fantasy.smallweather.db.SmallWeatherDB;
import com.fantasy.smallweather.model.Area;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 解析和处理从服务器接收到的数据
 * @author Fantasy
 * @version 1.0, 2016/8/25.
 */
public class Utility {

    /**
     * 解析服务器返回的JSON格式的地区信息，并将解析出的数据存储到数据库中
     */
    public synchronized static boolean handleAreaResponse(SmallWeatherDB smallWeatherDB,
                                                          String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONObject(response).getJSONArray("city_info");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject city_info = jsonArray.getJSONObject(i);
                    Area area = new Area();
                    String area_name = city_info.getString("city");
                    String area_code = city_info.getString("id");
                    area.setAreaName(area_name);
                    area.setAreaCode(area_code);
                    smallWeatherDB.saveArea(area);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 解析服务器返回的JSON格式的天气信息，并将解析出的数据存储到SharePreference文件中
     */
    public synchronized static boolean handleWeatherResponse(
            SharedPreferences.Editor editor, String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                // 先把JSON数据加载成数组，因为根部HeWeather data service 3.0后面是[符号，
                // 说明是以数组形式存放，只是这个数组里面只有一个元素
                JSONArray jsonArray = new JSONObject(response)
                        .getJSONArray("HeWeather data service 3.0");
                // 那么既然知道这个数组里面只有一个元素，所以我们直接取出第一个元素为JSONObject
                JSONObject weatherInfoAll = jsonArray.getJSONObject(0);
                //首先，我们看到，城市名称和数据更新的时间是在basic下面，所以可以直接获取
                JSONObject weatherInfoBasic = weatherInfoAll.getJSONObject("basic");
                /*"basic": {
                    "city": "北京",
                    "cnty": "中国",
                    "id": "CN101010100",
                    "lat": "39.904000",
                    "lon": "116.391000",
                    "update":
                    {
                       "loc": "2016-06-30 08:51",
                       "utc": "2016-06-30 00:51"
                    }
                },*/
                // 我们发现，有city和update，其中，city可以直接通过名称获取到信息
                editor.putString("area_name", weatherInfoBasic.getString("city"));
                editor.putString("area_code", weatherInfoBasic.getString("id"));
                // 但是，更新的时间是不能获取的，因为这里update后面是｛｝，表明这是一个对象
                // 所以先根据名称获取这个对象
                JSONObject weatherInfoBasicUpdate = weatherInfoBasic
                        .getJSONObject("update");
                // 然后再根据这个对象获取名称是loc的数据信息
                editor.putString("publish_time", weatherInfoBasicUpdate.getString("loc"));

                // 关于天气的所有信息都是在daily_forecast名称下面，
                // 仔细查看，发现daily_forecast后面是[符号，说明，这也是一个JSON数组
                // 所以先根据名称获取JSONArray对象
                JSONArray weatherInfoDailyForecast = weatherInfoAll
                        .getJSONArray("daily_forecast");
                //我们发现，[]里面是由很多个像下面这样的元素组成的
                /*
                {
                    "astro": {
                        "sr": "04:49",
                        "ss": "19:47"
                    },
                    "cond": {
                        "code_d": "302",
                        "code_n": "302",
                        "txt_d": "雷阵雨",
                        "txt_n": "雷阵雨"
                    },
                    "date": "2016-06-30",
                    "hum": "30",
                    "pcpn": "0.2",
                    "pop": "39",
                    "pres": "1002",
                    "tmp": {
                        "max": "31",
                        "min": "22"
                    },
                    "vis": "10",
                    "wind": {
                          "deg": "204",
                          "dir": "无持续风向",
                          "sc": "微风",
                          "spd": "4"
                    }
                },
                */

                // 第一个元素是当前的日期相关的天气数据，
                // 目前我们只需要第一个，并且获取出来的是一个JSONObject
                JSONObject weatherInfoNowForecast = weatherInfoDailyForecast
                        .getJSONObject(0);
                // 你会发现，date是可以直接获取的，因为date后面是没有｛｝的
                editor.putString("current_date", weatherInfoNowForecast.getString("date"));
                // tmp节点是当天的温度范围，包含最低和最高，说明这是一个JSONObject
                JSONObject weatherInfoNowForecastTmp = weatherInfoNowForecast
                        .getJSONObject("tmp");
                editor.putString("tmp_min", weatherInfoNowForecastTmp.getString("min"));
                editor.putString("tmp_max", weatherInfoNowForecastTmp.getString("max"));

                // cond是当前的实际天气描述，获取方法和tmp是一样的
                JSONObject weatherInfoNowForecastCond = weatherInfoNowForecast
                        .getJSONObject("cond");
                // 天气情况前
                editor.putString("txt_d", weatherInfoNowForecastCond.getString("txt_d"));
                // 天气情况后
                editor.putString("txt_n", weatherInfoNowForecastCond.getString("txt_n"));

                editor.commit();
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
