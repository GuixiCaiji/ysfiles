package com.java.yesheng;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Notification;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;


public class GraphActivity extends AppCompatActivity {

    private int cnt = 1;

    class Info {
        int CONFIRMED;
        int CURED;
        int DEAD;

        public Info(String confirmed, String cured, String dead) {
            CONFIRMED = Integer.valueOf(confirmed);
            CURED = Integer.valueOf(cured);
            DEAD = Integer.valueOf(dead);
        }

        public void add(String confirmed, String cured, String dead) {
            CONFIRMED += Integer.valueOf(confirmed);
            CURED += Integer.valueOf(cured);
            DEAD += Integer.valueOf(dead);
        }
    }

    private HashMap<String, Info> Global = new HashMap<>();
    private HashMap<String, Info> China = new HashMap<>();
    private Iterator<HashMap.Entry<String, Info>> iter1 = Global.entrySet().iterator();
    private Iterator<HashMap.Entry<String, Info>> iter2 = Global.entrySet().iterator();

    private ColumnChartView ColumnChartView;
    private ColumnChartData mColumnChartData;    //柱状图数据


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        androidx.appcompat.app.ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        init();
        initDate();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.graph_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
            return true;
        }
        else if(id == R.id.global_menu){
            initDate();
        }
        else if(id == R.id.province_menu){
            initDate2();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initDate() {
        iter1 = Global.entrySet().iterator();
        iter2 = China.entrySet().iterator();
        List<Column> columnList = new ArrayList<>(); //柱子列表
        List<SubcolumnValue> subcolumnValueList;     //子柱列表
        List<AxisValue> axisValues = new ArrayList<>();//创建x轴数据
        int i = 0;
        while (iter1.hasNext()) {
            HashMap.Entry<String, Info> entry = iter1.next();
            String xValues = entry.getKey();
            subcolumnValueList = new ArrayList<>();//每个子柱的集合
            subcolumnValueList.add(new SubcolumnValue(entry.getValue().CONFIRMED, ChartUtils.pickColor()));
            axisValues.add(new AxisValue(i++).setLabel(xValues));
            Column column = new Column(subcolumnValueList);//创建子柱数据
            column.setHasLabels(true);                    //设置列标签
            columnList.add(column);
        }
        mColumnChartData = new ColumnChartData(columnList);        //设置数据
        Axis axisX = new Axis(axisValues);//设置横坐标柱子下面的分类
        Axis axisY = new Axis().setHasLines(true);
        axisX.setHasTiltedLabels(true);
        axisX.setMaxLabelChars(2);
        axisY.setName("人数");
        mColumnChartData.setAxisXBottom(axisX); //设置横轴
        mColumnChartData.setAxisYLeft(axisY);   //设置竖轴
        ColumnChartView = findViewById(R.id.columnchart);
        //ColumnChartView.setInteractive(true);
        ColumnChartView.setZoomEnabled(false);//不可点击
        ColumnChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);

        ColumnChartView.setColumnChartData(mColumnChartData);

        Viewport v = new Viewport(ColumnChartView.getMaximumViewport());
        v.bottom = 0;
        v.top = 100000;
        ColumnChartView.setMaximumViewport(v);

        v.left = 0;
        v.right= 1;
        ColumnChartView.setCurrentViewport(v);
    }


    private void initDate2() {
        iter1 = Global.entrySet().iterator();
        iter2 = China.entrySet().iterator();
        List<Column> columnList = new ArrayList<>(); //柱子列表
        List<SubcolumnValue> subcolumnValueList;     //子柱列表
        List<AxisValue> axisValues = new ArrayList<>();//创建x轴数据
        int i = 0;
        while (iter2.hasNext()) {
            HashMap.Entry<String, Info> entry = iter2.next();
            String xValues = entry.getKey();
            subcolumnValueList = new ArrayList<>();//每个子柱的集合

            subcolumnValueList.add(new SubcolumnValue(entry.getValue().CONFIRMED, ChartUtils.COLOR_RED));
            subcolumnValueList.add(new SubcolumnValue(entry.getValue().CURED, ChartUtils.COLOR_GREEN));
            subcolumnValueList.add(new SubcolumnValue(entry.getValue().DEAD, ChartUtils.COLOR_BLUE));

            axisValues.add(new AxisValue(i++).setLabel(xValues));
            Column column = new Column(subcolumnValueList);//创建子柱数据
            column.setHasLabels(true);                    //设置列标签
            columnList.add(column);
        }
        mColumnChartData = new ColumnChartData(columnList);        //设置数据
        Axis axisX = new Axis(axisValues);//设置横坐标柱子下面的分类
        Axis axisY = new Axis().setHasLines(true);
        axisX.setHasTiltedLabels(true);
        axisX.setMaxLabelChars(6);
        axisY.setName("人数");
        mColumnChartData.setAxisXBottom(axisX); //设置横轴
        mColumnChartData.setAxisYLeft(axisY);   //设置竖轴
        ColumnChartView = findViewById(R.id.columnchart);
        //ColumnChartView.setInteractive(true);
        ColumnChartView.setZoomEnabled(false);//不可点击
        ColumnChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);

        ColumnChartView.setColumnChartData(mColumnChartData);

        Viewport v = new Viewport(ColumnChartView.getMaximumViewport());
        v.bottom = 0;
        v.top = 2000;
        ColumnChartView.setMaximumViewport(v);

        v.left = 0;
        v.right= 5;
        ColumnChartView.setCurrentViewport(v);
    }


    private void showGraph() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initDate();
            }
        });
    }

    protected void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getHTML();
            }
        }).start();
    }

    private void getHTML() {
        try {
            URL urlObj = new URL("https://covid-dashboard.aminer.cn/api/dist/epidemic.json");
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setConnectTimeout(5000);
            // 设置请求类型为Get类型
            connection.setRequestMethod("GET");
            // 判断请求Url是否成功
            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("url fail");
            }

            InputStream is = connection.getInputStream();

            byte[] buffer = new byte[4096];
            StringBuffer stringBuffer = new StringBuffer();
            int ret = is.read(buffer);
            while (ret >= 0) {
                if (ret > 0) {
                    String html = new String(buffer, 0, ret);
                    //Log.i("html", html);
                    stringBuffer.append(html);
                    ret = is.read(buffer);
                }
            }
            is.close();
            //Log.i("html",stringBuffer.toString());
            String info = stringBuffer.toString();
            JSONObject jsonObject = new JSONObject(info);
            //通过迭代器获得json当中所有的key值
            Iterator keys = jsonObject.keys();
            //然后通过循环遍历出的key值
            while (keys.hasNext()) {

                String key = String.valueOf(keys.next());
                JSONObject ele = jsonObject.getJSONObject(key);
                JSONArray jsonArray = new JSONArray(ele.getString("data"));
                String exact = jsonArray.getString(jsonArray.length() - 1);
                exact = exact.substring(1, exact.length() - 1);
                String[] dataset = exact.split(",");
                String count_key = key.split("\\|")[0];
                if (!Global.containsKey(count_key)) {
                    Global.put(count_key, new Info(dataset[0], dataset[2], dataset[3]));
                } else {
                    Global.get(count_key).add(dataset[0], dataset[2], dataset[3]);
                }
                if(count_key.equals("China")){
                    if(key.split("\\|").length>=2){
                        String province_key = key.split("\\|")[1];
                        if (!China.containsKey(province_key)) {
                            China.put(province_key, new Info(dataset[0], dataset[2], dataset[3]));
                        } else {
                           China.get(province_key).add(dataset[0], dataset[2], dataset[3]);
                        }
                    }
                }

            }

            /*for (String s : Global.keySet()) {
                Log.i("map", s + " " + Global.get(s).CONFIRMED + " " + Global.get(s).CURED + " " + Global.get(s).DEAD);
            }*/

            showGraph();
            /*JSONArray jsonArray = new JSONArray(info);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String content = obj.getString("title");
                //Log.i("html",content);
            }*/
            //Log.i("html", n);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
