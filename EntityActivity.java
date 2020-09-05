package com.java.yesheng;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
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

public class EntityActivity extends AppCompatActivity {

    private class Detail {
        public HashMap<String, String> details;

        public Detail() {
            details = new HashMap<>();
        }
    }

    private ListView EntityLV;
    private OneExpandAdapter adapter;

    private ArrayList<String> label = new ArrayList<>();
    private ArrayList<String> label_description = new ArrayList<>();
    private ArrayList<Detail> label_relation = new ArrayList<>();
    private ArrayList<Detail> label_property = new ArrayList<>();
    private ArrayList<Bitmap> label_pic = new ArrayList<>();

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
        setContentView(R.layout.activity_entity);
        androidx.appcompat.app.ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        SearchView mSearchView = (SearchView) findViewById(R.id.search);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQueryHint("请输入要查找的实体");

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            public boolean onQueryTextSubmit(String query) {
                //Toast.makeText(EntityActivity.this, query, Toast.LENGTH_SHORT).show();
                label.clear();
                label_description.clear();
                label_property.clear();
                label_relation.clear();
                label_pic.clear();
                init(query);
                return true;
            }

            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        requestData();

    }

    private void requestData() {
        ArrayList<HashMap<String, String>> datas = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < label.size(); i++) {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put("Entity", label.get(i));
            item.put("Description", label_description.get(i));

            String output0 = "";
            for (HashMap.Entry<String, String> entry : label_relation.get(i).details.entrySet()) {
                output0 += entry.getValue() + " -> " + entry.getKey() + "\n";
            }
            item.put("Relation", output0);

            String output = "";
            for (HashMap.Entry<String, String> entry : label_property.get(i).details.entrySet()) {
                output += entry.getKey() + " : " + entry.getValue() + "\n";
            }
            item.put("Property", output);
            datas.add(item);

        }

        EntityLV = (ListView) findViewById(R.id.EntityListView);
        adapter = new OneExpandAdapter(this, datas);
        EntityLV.setAdapter(adapter);
    }

    private static class MYViewHolder {
        private LinearLayout showArea;

        private TextView Entity;
        private TextView Description;
        private TextView Relation;
        private TextView Property;
        private ImageView Pic;

        private LinearLayout hideArea;
    }


    public class OneExpandAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<HashMap<String, String>> list;
        private int currentItem = -1; //用于记录点击的 Item 的 position

        public OneExpandAdapter(Context context, ArrayList<HashMap<String, String>> list) {
            super();
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            MYViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.entity_layout, parent, false);
                holder = new MYViewHolder();

                holder.showArea = (LinearLayout) convertView.findViewById(R.id.layout_showArea);
                holder.Entity = (TextView) convertView.findViewById(R.id.entity_name);
                holder.Description = (TextView) convertView.findViewById(R.id.entity_description);
                holder.Relation = (TextView) convertView.findViewById(R.id.entity_relation);
                holder.Property = (TextView) convertView.findViewById(R.id.entity_property);
                holder.Pic = (ImageView) convertView.findViewById(R.id.label_pic);


                holder.hideArea = (LinearLayout) convertView.findViewById(R.id.layout_hideArea);

                convertView.setTag(holder);
            } else {
                holder = (MYViewHolder) convertView.getTag();
            }

            HashMap<String, String> item = list.get(position);

            holder.showArea.setTag(position);

            holder.Entity.setText(item.get("Entity"));
            holder.Description.setText(item.get("Description"));
            holder.Relation.setText(item.get("Relation"));
            holder.Property.setText(item.get("Property"));
            if (label_pic.get(position) != null) {
                holder.Pic.setImageBitmap(label_pic.get(position));
            } else {
                holder.Pic.setImageBitmap(null);
            }


            if (currentItem == position) {
                holder.hideArea.setVisibility(View.VISIBLE);
            } else {
                holder.hideArea.setVisibility(View.GONE);
            }

            holder.showArea.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int tag = (Integer) view.getTag();
                    if (tag == currentItem) { //再次点击
                        currentItem = -1; //给 currentItem 一个无效值
                    } else {
                        currentItem = tag;
                    }
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }


    }


    public void init(final String urlstr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getHTML("https://innovaapi.aminer.cn/covid/api/v1/pneumonia/entityquery?entity=" + urlstr);
            }
        }).start();
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
                JSONObject information = jsonArray.getJSONObject(i);
                JSONObject ab_info = information.getJSONObject("abstractInfo");
                String des = ab_info.getString("enwiki") + ab_info.getString("baidu") + ab_info.getString("zhwiki");
                String name = jsonArray.getJSONObject(i).getString("label");
                label.add(name);
                label_description.add(des);

                String relation_str = ab_info.getJSONObject("COVID").getString("relations");
                JSONArray relation_Array = new JSONArray(relation_str);
                Detail det2 = new Detail();
                for (int j = 0; j < relation_Array.length(); j++) {
                    String key = relation_Array.getJSONObject(j).getString("label");
                    String value = relation_Array.getJSONObject(j).getString("relation");
                    det2.details.put(key, value);
                }
                label_relation.add(det2);

                JSONObject properties = ab_info.getJSONObject("COVID").getJSONObject("properties");
                Iterator<String> it = properties.keys();
                Detail det = new Detail();
                while (it.hasNext()) {
                    String key = it.next();
                    String value = properties.getString(key);
                    det.details.put(key, value);
                }
                label_property.add(det);

                String img_url = information.getString("img");
                if (!img_url.equals("null")) {
                    URL img_obj = new URL(img_url);
                    HttpURLConnection img_conn = (HttpURLConnection) img_obj.openConnection();
                    img_conn.connect();
                    InputStream imgis = img_conn.getInputStream();
                    Bitmap bm = BitmapFactory.decodeStream(imgis);
                    label_pic.add(bm);
                } else {
                    label_pic.add(null);
                }


            }
            showEntity();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showEntity() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                requestData();
            }
        });
    }

}
