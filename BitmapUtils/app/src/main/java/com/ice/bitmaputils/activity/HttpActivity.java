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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

public class HttpActivity extends Activity {

    private static final String TAG = "HttpActivity";
    private OkHttpClient mClient;
    private ListView mListView;
    private Call mCall;

    private LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(1*1024*1024) {
        @Override
        protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };


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
        String jsonStr = getJsonFromAssets(this,"app_info_json.json");
        ArrayList<AppInfoData> mDatas = ParseJsonList(jsonStr);
        NetAdapter mAdapter = new NetAdapter(getApplicationContext(), mDatas);
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mReceiver!=null) {
            IntentFilter mfilter = new IntentFilter();
            mfilter.addAction("com.http.progress.refresh");
            registerReceiver(mReceiver,mfilter);
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
        if(mCall!=null && !mCall.isCanceled()) {
            mCall.cancel();
        }

        if(mCache!=null) {
            mCache.evictAll();
        }
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
            }
        }
    }

    /**
     * load image by url, if memory hasn't cache load local,
     * local cache has it get local otherwise load remote
     * @param url remote image url
     * **/
    private void loadRemoteOrLocalImage(final String url){
        try {
            if(mCache!=null && mCache.get(url)!=null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                        if(imageView!=null) {
                            imageView.setImageBitmap(mCache.get(url));
                        }
                    }
                });
                return;
            }
            String path_md5 = FileUtils.ParseMd5(url);
            File file = new File("/storage/emulated/0/my_app_cache/" + path_md5 + ".webp");
            Log.d(TAG,"loadRemoteImage file: "+(file!=null?file.exists():null));
            if (file!=null && file.exists()) {
                getLocalImage(url,path_md5);
                return;
            } else {
                getRemoteImage(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * method for get local cache image if file already exit
     * @param url origin remote url
     * @param path md5 format remonte url
     * **/
    private void getLocalImage(final String url,String path){
        final Bitmap file_bitmap = BitmapUtils.createScaleBitmapFromFile("/storage/emulated/0/my_app_cache/" + path + ".webp",
                100,100,null);
        if(mCache.get(url)==null) {
            mCache.put(url, file_bitmap);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"runOnUiThread file bitmap: "+(file_bitmap==null));
                ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                if(imageView!=null) {
                    imageView.setImageBitmap(file_bitmap);
                }
            }
        });
    }

    /**
     * method for get remote image by http if local no exit
     * @param url remote image url
     * **/
    private void getRemoteImage(final String url){
        if(mClient == null) {
            mClient = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
        }
        Request request = new Request.Builder()
                .addHeader("Accept-Encoding", "gzip, deflate")
                .url(url)
                .header("RANGE", "bytes="+0L+"-")
                .build();


        try {
            mCall = mClient.newCall(request);
            mCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG,"on http fail");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        Log.d(TAG,"on http response: "+response.isSuccessful()+" : "+response.code());
                        if (response.isSuccessful()) {
                            byte[] img_bytes = response.body().bytes();
                            final Bitmap bitmap = BitmapUtils.createScaleFromBytes(img_bytes, 100, 100,null);
                            response.body().close();
                            BitmapUtils.decodeBitmapToFile(bitmap,
                                    "/storage/emulated/0/my_app_cache/"+FileUtils.ParseMd5(url),BitmapUtils.TYPE_WEBP);
                            if(mCache.get(url)==null) {
                                mCache.put(url, bitmap);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG,"runOnUiThread: "+(bitmap==null));
                                    ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                                    if(imageView!=null) {
                                        imageView.setImageBitmap(bitmap);
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class NetAdapter extends BaseAdapter {

        private Context mContext;
        private List<AppInfoData> mList;
        private View.OnClickListener mOnClickListener;
        private PackageManager mPackageManager;

        public NetAdapter (Context context, List<AppInfoData> list) {
            mContext = context;
            mList = list;
            mPackageManager = (PackageManager)mContext.getApplicationContext()
                    .getPackageManager();
            mOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = (String)v.getTag();
                    Log.d(TAG, "start service url: "+url);
                    if(!TextUtils.isEmpty(url)) {
                        Intent intent = new Intent(getApplicationContext(), HttpService.class);
                        intent.putExtra("app_url", url);
                        startService(intent);
                    }
                }
            };
        }

        @Override
        public int getCount() {
            if(mList!=null) {
                return mList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if(mList!=null && position < mList.size()) {
                return mList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mHolder = null;
            if(convertView==null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item,null);
                mHolder = new ViewHolder();
                mHolder.mTitle = convertView.findViewById(R.id.title);
                mHolder.mIcon = convertView.findViewById(R.id.icon);
                mHolder.mSummary = convertView.findViewById(R.id.summary);
                mHolder.mVersion = convertView.findViewById(R.id.version);
                mHolder.mProgress = convertView.findViewById(R.id.progress_btn);
                convertView.setTag(mHolder);
            } else {
                mHolder = (ViewHolder)convertView.getTag();
            }

            AppInfoData data = (AppInfoData) getItem(position);
            if(data != null) {
                mHolder.mTitle.setText(data.getName());
                mHolder.mSummary.setText(data.getDescri());
                mHolder.mVersion.setText(data.getVersion());

                boolean isInstalled = Utils.isAppInstalled(mPackageManager,data.getPackageName(),
                        data.getVersion());
                mHolder.mProgress.setCurrentProgress(isInstalled?100:-1, true);
                mHolder.mProgress.setTag(data.getAppUrl());
                if(isInstalled) {
                    mHolder.mProgress.setOnClickListener(null);
                } else {
                    mHolder.mProgress.setOnClickListener(mOnClickListener);
                }

                String url = data.getUrl();
                mHolder.mIcon.setTag(url);
                if (mCache.get(url) == null) {
                    loadRemoteOrLocalImage(url);
                } else {
                    mHolder.mIcon.setImageBitmap(mCache.get(url));
                }
            }

            return convertView;
        }

        class ViewHolder{
            TextView mTitle;
            TextView mSummary;
            TextView mVersion;
            ImageView mIcon;
            ProgressButton mProgress;
        }
    }
}
