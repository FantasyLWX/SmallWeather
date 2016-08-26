package com.fantasy.smallweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.fantasy.smallweather.R;
import com.fantasy.smallweather.db.SmallWeatherDB;
import com.fantasy.smallweather.model.Area;
import com.fantasy.smallweather.util.HttpCallbackListener;
import com.fantasy.smallweather.util.HttpUtil;
import com.fantasy.smallweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fantasy
 * @version 1.0, 2016/8/24
 */
public class ChooseAreaActivity extends Activity {

    /** 我的和风天气的API的KEY */
    public static final String WEATHER_KEY = "2816d66ea029410683329d65253d3f8e";
    private SearchView searchView;
    private ListView listView;
    private ProgressDialog progressDialog;
    private SmallWeatherDB smallWeatherDB;
    private ArrayAdapter<String> adapter;
    /** 存储地区名 */
    private List<String> areaNameList = new ArrayList<>();
    /** 地区列表，存储地区对象 */
    private List<Area> areaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_area_layout);
        searchView = (SearchView) findViewById(R.id.search_view);
        listView = (ListView) findViewById(R.id.list_view);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                areaNameList);
        listView.setAdapter(adapter);
        smallWeatherDB = SmallWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
            }
        });
        queryAreas(); // 加载全中国各地区的信息
    }
    /**
     * 查询全中国所有地区，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryAreas() {
        areaList = smallWeatherDB.loadAreas();
        if (areaList.size() > 0) {
            areaNameList.clear();
            for (Area area : areaList) {
                areaNameList.add(area.getAreaName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
        } else {
            queryFromServer();
        }
    }
    /**
     * 根据传入的代号和类型从服务器上查询地区信息
     */
    private void queryFromServer() {
        String address = "https://api.heweather.com/x3/citylist?search=allchina&key=" +
                WEATHER_KEY;
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if (Utility.handleAreaResponse(smallWeatherDB, response)) {
                    // 通过runOnUiThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            queryAreas();
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
                        Toast.makeText(ChooseAreaActivity.this, "加载失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

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
}
