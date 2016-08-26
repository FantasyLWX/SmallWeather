package com.fantasy.smallweather.util;

import android.text.TextUtils;

import com.fantasy.smallweather.db.SmallWeatherDB;
import com.fantasy.smallweather.model.Area;

import org.json.JSONArray;
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
}
