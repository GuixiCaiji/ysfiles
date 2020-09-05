package com.java.yesheng;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.widget.Toast;


public class NewsListActivity extends AppCompatActivity {

    private RecyclerView newsListView;

    private List<String> NewsData = new ArrayList<>();

    private MyAdapter adapter = new MyAdapter();
    private SwipeRefreshLayout mRefreshLayout;
    private int count = 1;

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //使用菜单填充器获取menu下的菜单资源文件
        getMenuInflater().inflate(R.menu.menue, menu);
        //获取搜索的菜单组件
        MenuItem menuItem = menu.findItem(R.id.search);
        //设置搜索的事件
        return super.onCreateOptionsMenu(menu);
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(NewsListActivity.this);
            MyViewHolder viewHolder = new MyViewHolder(textView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            TextView textView = (TextView) holder.itemView;
            String text = NewsData.get(position);
            textView.setText(text);
        }

        @Override
        public int getItemCount() {
            return NewsData.size();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);


        androidx.appcompat.app.ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        newsListView = (RecyclerView) findViewById(R.id.newsListView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        newsListView.setLayoutManager(layoutManager);
        newsListView.setAdapter(adapter);
        init("https://covid-dashboard.aminer.cn/api/events/list?type=paper&page=" + count++);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.layout_swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                NewsData.clear();
                init("https://covid-dashboard.aminer.cn/api/events/list?type=paper&page=" + count++);
                mRefreshLayout.setRefreshing(false);
            }
        });
    }

    protected void init(final String urlstr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getHTML(urlstr);
            }
        }).start();
    }

    private void showNews() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void getHTML(final String urlstr) {
        try {
            URL urlObj = new URL(urlstr);
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
            JSONObject jsonobj = new JSONObject(info);
            String data = jsonobj.getString("data");
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String content = obj.getString("title");
                //Log.i("html",content);
                NewsData.add(content);
            }
            showNews();
            //Log.i("html", n);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
