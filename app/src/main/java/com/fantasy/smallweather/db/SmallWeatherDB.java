package com.fantasy.smallweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fantasy.smallweather.model.Area;

import java.util.ArrayList;
import java.util.List;

/**
 * 单例类，封装了一些常用的数据库操作
 * @author Fantasy
 * @version 1.0, 2016/8/25.
 */
public class SmallWeatherDB {

    /** 数据库名 */
    public static final String DB_NAME = "small_weather";
    /** 数据库版本 */
    public static final int VERSION = 1;
    /** 单例对象 */
    private static SmallWeatherDB smallWeatherDB;
    private SQLiteDatabase db;

    /**
     * 将构造方法私有化
     */
    private SmallWeatherDB(Context context) {
        SmallWeatherOpenHelper dbHelper = new SmallWeatherOpenHelper(context,
                DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }
    /**
     * 获取SmallWeatherDB的实例
     */
    public synchronized static SmallWeatherDB getInstance(Context context) {
        if (smallWeatherDB == null) {
            smallWeatherDB = new SmallWeatherDB(context);
        }
        return smallWeatherDB;
    }
    /**
     * 将Area实例存储到数据库中
     */
    public void saveArea(Area area) {
        if (area != null) {
            ContentValues values = new ContentValues();
            values.put("area_name", area.getAreaName());
            values.put("area_code", area.getAreaCode());
            db.insert("Area", null, values);
        }
    }
    /**
     * 从数据库读取全国所有的地区
     */
    public List<Area> loadAreas() {
        List<Area> areaList = new ArrayList<>();
        Cursor cursor = db.query("Area", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Area area = new Area();
                area.setId(cursor.getInt(cursor.getColumnIndex("id")));
                area.setAreaName(cursor.getString(cursor.getColumnIndex("area_name")));
                area.setAreaCode(cursor.getString(cursor.getColumnIndex("area_code")));
                areaList.add(area);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return areaList;
    }
    /**
     * 根据关键字，从数据库获取匹配的地区
     */
    public List<Area> loadAreasByKey(String key) {
        List<Area> areaList = new ArrayList<>();
        Cursor cursor = db.query("Area", null, "area_name like ?",
                new String[]{"%" + key + "%"}, null, null, "area_code");
        if (cursor.moveToFirst()) {
            do {
                Area area = new Area();
                area.setId(cursor.getInt(cursor.getColumnIndex("id")));
                area.setAreaName(cursor.getString(cursor.getColumnIndex("area_name")));
                area.setAreaCode(cursor.getString(cursor.getColumnIndex("area_code")));
                areaList.add(area);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return areaList;
    }
}
