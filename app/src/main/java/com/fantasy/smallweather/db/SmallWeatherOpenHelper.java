package com.fantasy.smallweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类，提供创建和升级数据库的功能
 * @author Fantasy
 * @version 1.0, 2016/8/25.
 */
public class SmallWeatherOpenHelper extends SQLiteOpenHelper {

    /** Area表的建表语句 */
    public static final String CREATE_AREA = "create table Area (" +
            "id integer primary key autoincrement," +
            "area_name text," +
            "area_code text)";

    public SmallWeatherOpenHelper(Context context, String name,
                                  SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_AREA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
