package com.ice.bitmaputils.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.ice.bitmaputils.R;
import com.ice.bitmaputils.data.AppInfoData;
import com.ice.bitmaputils.services.HttpService;
import com.ice.bitmaputils.utils.BitmapUtils;
import com.ice.bitmaputils.utils.FileUtils;
import com.ice.bitmaputils.utils.Utils;
import com.ice.bitmaputils.view.ProgressButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by minwalker on 2018/3/10.
 */

public class HttpActivity extends BaseHttpActivity implements View.OnClickListener,AdapterView.OnItemClickListener{

    private static final String TAG = "HttpActivity";
    private ListView mListView;
    private View mCardOne,mCardTwo,mCardThree;
    private View mTopEnter,mVedioEnter,mGameEnter,mCameraEnter;
    private NetAdapter mAdapter;
    private ArrayList<AppInfoData> mListDatas;
    private ArrayList<AppInfoData> mTopDatas;
    private static final boolean isGlide = false;


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String app_url = intent.getStringExtra("app_url");
            if(mListView!=null) {
                ProgressButton mProgressBtn = (ProgressButton)mListView.findViewWithTag(app_url);
                if(mProgressBtn!=null) {
                    int progress = intent.getIntExtra("progress",0);
                    mProgressBtn.setCurrentProgress(progress,false);
                }
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_layout);
        mListView = findViewById(R.id.list_view);
        mCardOne = findViewById(R.id.card_one);
        mCardTwo = findViewById(R.id.card_two);
        mCardThree = findViewById(R.id.card_three);
        mTopEnter = findViewById(R.id.type_top);
        mVedioEnter = findViewById(R.id.type_video);
        mGameEnter = findViewById(R.id.type_game);
        mCameraEnter = findViewById(R.id.type_camera);
        String jsonStr = getJsonFromAssets(this,"app_info_json.json");
        String topStr = getJsonFromAssets(this, "top_app_info_json.json");
        mListDatas = ParseJsonList(jsonStr);
        mTopDatas = ParseJsonList(topStr);
        initCardTag(mTopDatas);
        mAdapter = new NetAdapter(getApplicationContext(), mListDatas, mListView);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mCardOne.setOnClickListener(this);
        mCardTwo.setOnClickListener(this);
        mCardThree.setOnClickListener(this);
        mTopEnter.setOnClickListener(this);
        mVedioEnter.setOnClickListener(this);
        mGameEnter.setOnClickListener(this);
        mCameraEnter.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mReceiver!=null) {
            IntentFilter mfilter = new IntentFilter();
            mfilter.addAction("com.http.progress.refresh");
            registerReceiver(mReceiver,mfilter);
        }

        if(mAdapter!=null && mAdapter.getCount() > 0) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mReceiver!=null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.card_one:
            case R.id.card_two:
            case R.id.card_three:
                Intent intent = new Intent(HttpActivity.this,AppInfoActivity.class);
                intent.putExtra("app_info",(AppInfoData)view.getTag());
                startActivity(intent);
                break;
            case R.id.type_top:
            case R.id.type_video:
            case R.id.type_game:
            case R.id.type_camera:
                Intent typeIntent = new Intent(HttpActivity.this,AppListActivity.class);
                ArrayList<AppInfoData> datas = new ArrayList<AppInfoData>();
                datas.addAll(mTopDatas);
                datas.addAll(mListDatas);
                typeIntent.putExtra("app_type",view.getId());
                typeIntent.putExtra("apps_list",datas);
                startActivity(typeIntent);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        AppInfoData data = mAdapter.getItem(position);
        if(data!=null) {
            Intent intent = new Intent(HttpActivity.this, AppInfoActivity.class);
            intent.putExtra("app_info",data);
            startActivity(intent);
        }
    }

    private void initCardTag(List datas) {
        mCardOne.setTag(datas.get(0));
        mCardTwo.setTag(datas.get(1));
        mCardThree.setTag(datas.get(2));
    }

    /**
     * method using to get json String from Assets file
     * @param context Activity or Application
     * @param filePath file path of json file
     * **/
    private String getJsonFromAssets(Context context, String filePath) {
        StringBuilder builder = new StringBuilder();
        BufferedReader bf = null;
        try {
            AssetManager assetManager = context.getAssets();
            bf = new BufferedReader(new InputStreamReader(assetManager.open(filePath)));
            String line;
            while((line = bf.readLine())!=null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bf != null) {
                    bf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return builder.toString();
    }

    /**
     * method to parse json into AppInfoData class
     * we need to follow the json file format by using
     * JsonReader.beginObject(), nextName(), nextString()
     * or you will get a crash
     * @param JsonStr origin json string
     * @return a list of app info datas
     * **/
    public ArrayList<AppInfoData> ParseJsonList(String JsonStr){
        ArrayList<AppInfoData> mList = new ArrayList<AppInfoData>();
        JsonReader jsReader = new JsonReader(new StringReader(JsonStr));
        try {
            jsReader.beginObject();
            while(jsReader.hasNext()) {
                AppInfoData data = new AppInfoData();
                jsReader.nextName();
                jsReader.beginObject();

                readDataWithTag(jsReader,"name", data);
                readDataWithTag(jsReader,"ver", data);
                readDataWithTag(jsReader,"pk_name", data);
                readDataWithTag(jsReader,"img_url", data);
                readDataWithTag(jsReader,"desr", data);
                readDataWithTag(jsReader,"app_url", data);
                readDataWithTag(jsReader,"app_size", data);
                readDataWithTag(jsReader,"app_rate", data);
                readDataWithTag(jsReader,"app_type", data);
                readDataWithTag(jsReader,"img_list", data);

                if(!mList.contains(data)) {
                    mList.add(data);
                }
                jsReader.endObject();
            }
            jsReader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mList;
    }

    /**
     * method for read app info data from json
     * @param jsReader JsonReader Object
     * @param name tag name of we want
     * @param data AppInfoData we create to save info
     * **/
    private void readDataWithTag(JsonReader jsReader,
                                 String name, AppInfoData data) throws IOException {
        String tag = jsReader.nextName();
        if(name.equals(tag)) {
            if(name.equals("img_list")) {
                jsReader.beginArray();
                ArrayList<String> imageList = new ArrayList<String>();
                while (jsReader.hasNext()) {
                    tag = jsReader.nextString();
                    if(!imageList.contains(tag)) {
                        imageList.add(tag);
                    }
                }
                jsReader.endArray();
                data.setImageList(imageList);
            } else {
                tag = jsReader.nextString();
                switch (name) {
                    case "name":
                        data.setName(tag);
                        break;
                    case "ver":
                        data.setVersion(tag);
                        break;
                    case "img_url":
                        data.setUrl(tag);
                        break;
                    case "desr":
                        data.setDescri(tag);
                        break;
                    case "app_url":
                        data.setAppUrl(tag);
                        break;
                    case "pk_name":
                        data.setPackageName(tag);
                        break;
                    case "app_size":
                        data.setAppSize(tag);
                        break;
                    case "app_rate":
                        data.setAppRate(Float.valueOf(tag));
                        break;
                    case "app_type":
                        data.setAppType(Integer.valueOf(tag));
                        break;
                }
            }
        }
    }

}
