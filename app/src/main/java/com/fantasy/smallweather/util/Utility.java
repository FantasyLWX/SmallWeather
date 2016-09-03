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
 * @version 1.1, 2016/09/03
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
                editor.putBoolean("area_selected", true);
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
//                JSONObject weatherInfoBasicUpdate = weatherInfoBasic
//                        .getJSONObject("update");
                // 然后再根据这个对象获取名称是loc的数据信息
//                editor.putString("publish_time", weatherInfoBasicUpdate.getString("loc"));

                // 关于天气的所有信息都是在daily_forecast名称下面，
                // 仔细查看，发现daily_forecast后面是[符号，说明，这也是一个JSON数组
                // 所以先根据名称获取JSONArray对象
                JSONArray dailyForecast = weatherInfoAll.getJSONArray("daily_forecast");
                //我们发现，[]里面是由很多个像下面这样的元素组成的
                /*{
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
                },*/
                // 第一个元素是当前的日期相关的天气数据，
                // 我们先获取第一个，并且获取出来的是一个JSONObject
                JSONObject dailyForecast1 = dailyForecast.getJSONObject(0);
                // 你会发现，date是可以直接获取的，因为date后面是没有｛｝的
                editor.putString("date_1", dailyForecast1.getString("date"));
                // cond节点是当前的实际天气描述，后面跟有{}，所以这是一个JSONObject
                JSONObject dailyForecastCond1 = dailyForecast1.getJSONObject("cond");
                // txt_d白天天气描述   txt_n夜间天气描述
                if (dailyForecastCond1.getString("txt_d").equals(
                        dailyForecastCond1.getString("txt_n"))) {
                    editor.putString("weather_desp_1",
                            dailyForecastCond1.getString("txt_d"));
                } else {
                    editor.putString("weather_desp_1",
                            dailyForecastCond1.getString("txt_d")  + "转" +
                                    dailyForecastCond1.getString("txt_n"));
                }
                // tmp节点是当天的温度范围，包含最低和最高，获取方法和cond是一样的
                JSONObject dailyForecastTmp1 = dailyForecast1.getJSONObject("tmp");
                if (dailyForecastTmp1.getString("min").equals(
                        dailyForecastTmp1.getString("max"))) {
                    editor.putString("tmp_range_1", dailyForecastTmp1.getString("min") +
                            "℃");
                } else {
                    editor.putString("tmp_range_1", dailyForecastTmp1.getString("min") +
                            "℃ ~ " + dailyForecastTmp1.getString("max") + "℃");
                }

                // 我们获取第二个元素，即明天的天气预报
                JSONObject dailyForecast2 = dailyForecast.getJSONObject(1);
                editor.putString("date_2", dailyForecast2.getString("date"));
                JSONObject dailyForecastCond2 = dailyForecast2.getJSONObject("cond");
                if (dailyForecastCond2.getString("txt_d").equals(
                        dailyForecastCond2.getString("txt_n"))) {
                    editor.putString("weather_desp_2",
                            dailyForecastCond2.getString("txt_d"));
                } else {
                    editor.putString("weather_desp_2",
                            dailyForecastCond2.getString("txt_d")  + "转" +
                                    dailyForecastCond2.getString("txt_n"));
                }
                JSONObject dailyForecastTmp2 = dailyForecast2.getJSONObject("tmp");
                if (dailyForecastTmp2.getString("min").equals(
                        dailyForecastTmp2.getString("max"))) {
                    editor.putString("tmp_range_2", dailyForecastTmp2.getString("min") +
                            "℃");
                } else {
                    editor.putString("tmp_range_2", dailyForecastTmp2.getString("min") +
                            "℃ ~ " + dailyForecastTmp2.getString("max") + "℃");
                }

                // 我们获取第三个元素，即后天的天气预报
                JSONObject dailyForecast3 = dailyForecast.getJSONObject(2);
                editor.putString("date_3", dailyForecast3.getString("date"));
                JSONObject dailyForecastCond3 = dailyForecast3.getJSONObject("cond");
                if (dailyForecastCond3.getString("txt_d").equals(
                        dailyForecastCond3.getString("txt_n"))) {
                    editor.putString("weather_desp_3",
                            dailyForecastCond3.getString("txt_d"));
                } else {
                    editor.putString("weather_desp_3",
                            dailyForecastCond1.getString("txt_d")  + "转" +
                                    dailyForecastCond3.getString("txt_n"));
                }
                JSONObject dailyForecastTmp3 = dailyForecast3.getJSONObject("tmp");
                if (dailyForecastTmp3.getString("min").equals(
                        dailyForecastTmp3.getString("max"))) {
                    editor.putString("tmp_range_3", dailyForecastTmp3.getString("min") +
                            "℃");
                } else {
                    editor.putString("tmp_range_3", dailyForecastTmp3.getString("min") +
                            "℃ ~ " + dailyForecastTmp3.getString("max") + "℃");
                }

                // 我们接下来获取实时天气“now”，它是一个JSONObject
                /*"now": {
                    "cond": {
                        "code": "104",
                        "txt": "阴"
                    },
                    "fl": "21",		体感温度
                    "hum": "86",		湿度(%)
                    "pcpn": "0",
                    "pres": "1004",
                    "tmp": "22",		当前温度
                    "vis": "10",
                    "wind": {
                        "deg": "10",
                        "dir": "东北风",	      风向(方向)
                        "sc": "4-5",		风力等级
                        "spd": "19"
                    }
                },*/
                JSONObject now = weatherInfoAll.getJSONObject("now");
                // 直接获取体感温度fl
                editor.putString("now_fl", "体感温度：" + now.getString("fl") + "℃");
                // 直接获取湿度hum
                editor.putString("now_hum", "空气湿度：" + now.getString("hum") + "%");
                // 直接获取当前温度tmp
                editor.putString("now_tmp", now.getString("tmp") + "℃");
                // wind节点描述的是风力状况，因为后面跟有{}，所以这是一个JSONObject
                JSONObject nowWind = now.getJSONObject("wind");
                // 风向dir 风力等级sc
                editor.putString("now_wind", "风向风力：" + nowWind.getString("dir") +
                        nowWind.getString("sc") + "级");

                // 接下来获取空气质量指数api
                /*"aqi": {
                    "city": {
                        "aqi": "101",		空气质量指数
                        "co": "1",
                        "no2": "47",
                        "o3": "18",
                        "pm10": "40",
                        "pm25": "101",
                        "qlty": "良",		空气质量类别
                        "so2": "2"
                    }
                },*/
                // 因为部分地区没有空气质量数据，所以要判断一下
                if (weatherInfoAll.has("aqi")) {
                    JSONObject aqi = weatherInfoAll.getJSONObject("aqi");
                    JSONObject nowAqi = aqi.getJSONObject("city");
                    editor.putString("now_aqi", "空气质量：" + nowAqi.getString("aqi") +
                            " " + nowAqi.getString("qlty"));
                } else {
                    editor.putString("now_aqi", "");
                }

                editor.commit();
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
