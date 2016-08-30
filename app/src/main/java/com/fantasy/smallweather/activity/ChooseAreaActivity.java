package com.fantasy.smallweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
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
 * 选择地区
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
    /** 点击返回键的时间 */
    private long exitTime = 0;
    /** 选中的地区 */
    private Area areaSelected;
    /** 地区列表，存储地区对象 */
    private List<Area> areaList;
    /** ListView加载的数据列表，存储地区名 */
    private List<String> areaNameList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_area_layout);
        searchView = (SearchView) findViewById(R.id.search_view);
        listView = (ListView) findViewById(R.id.list_view);
        smallWeatherDB = SmallWeatherDB.getInstance(this);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                areaNameList);
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true); // 设置ListView启动过滤
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // areaList与areaNameList存储的地区是对应的，地区对象 → 地区名
//                Toast.makeText(ChooseAreaActivity.this,
//                        areaList.get(position).getAreaName(), Toast.LENGTH_SHORT).show();
                areaSelected = areaList.get(position);
                Intent intent = new Intent(ChooseAreaActivity.this,
                        WeatherActivity.class);
                intent.putExtra("area_code", areaList.get(position).getAreaCode());
                startActivity(intent);
                finish();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // 使用默认的操作，当点击软键盘的搜索按钮后，软键盘会自动关闭
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    queryAreas();
                } else {
                    areaList = smallWeatherDB.loadAreasByKey(newText);
                    areaNameList.clear();
                    for (Area area : areaList) {
                        areaNameList.add(area.getAreaName());
                    }
                    adapter.notifyDataSetChanged();
                    listView.setSelection(0);
                }
                // 提示功能，点击数据源，执行的操作是错的，因为ListView加载的数据没有更新
//                if (TextUtils.isEmpty(newText)) {
//                    adapter.getFilter().filter("");
//                    //listView.clearTextFilter(); // 如果输入内容过，ListView无法回到初始状态
//                } else {
//                    // 使用用户输入的内容对listView的列表项进行过滤
//                    adapter.getFilter().filter(newText);
//                    //listView.setFilterText(newText); // 输入后，会弹出Toast
//                }
                return true;
            }
        });
        queryAreas();
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
            queryAreaFromServer();
        }
    }
    /**
     * 从服务器上查询全中国所有地区的信息
     */
    private void queryAreaFromServer() {
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
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Toast.makeText(ChooseAreaActivity.this,
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
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
