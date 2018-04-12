package com.ice.bitmaputils.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.ice.bitmaputils.view.BackgroundView;
import com.ice.bitmaputils.view.ProgressButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by minwalker on 2018/4/11.
 */

public class BaseHttpActivity extends Activity {
    private static final String TAG = "BaseHttpActivity";
    protected OkHttpClient mClient;
    protected Call mCall;
    private UIHandler mHandler = new UIHandler();

    protected LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(1*1024*1024) {
        @Override
        protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHandler!=null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if(mCall!=null && !mCall.isCanceled()) {
            mCall.cancel();
        }

        if(mCache!=null) {
            mCache.evictAll();
        }
    }

    protected void loadRemoteOrLocalImage(final String url,final View parent, final int width, final int height) {
        loadRemoteOrLocalImage(url,parent,width,height,false);
    }

    /**
     * load image by url, if memory hasn't cache load local,
     * local cache has it get local otherwise load remote
     * @param url remote image url
     * **/
    protected synchronized void loadRemoteOrLocalImage(final String url,final View parent, final int width, final int height,boolean corner){
        try {
            if(mCache!=null && mCache.get(url)!=null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View imageView = parent.findViewWithTag(url);
                        if(imageView!=null) {
                            if (imageView instanceof ImageView) {
                                ((ImageView) imageView).setImageBitmap(mCache.get(url));
                            } else if (imageView instanceof BackgroundView) {
                                ((BackgroundView) imageView).setBitmap(mCache.get(url));
                            }
                        }
                    }
                });
                return;
            }
            String path_md5 = FileUtils.ParseMd5(url);
            File file = new File("/storage/emulated/0/my_app_cache/" + path_md5 + ".webp");
            Log.d(TAG,"loadRemoteImage file: "+(file!=null?file.exists():null));
            if (file!=null && file.exists()) {
                getLocalImage(url,path_md5,parent,width,height);
                return;
            } else {
                getRemoteImage(url,parent,width,height,corner);
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
    private void getLocalImage(final String url,String path,final View parent, final int width, final int height){
        if(TextUtils.isEmpty(url)) {
            return;
        }
        final Bitmap file_bitmap = BitmapUtils.createScaleBitmapFromFile("/storage/emulated/0/my_app_cache/" + path + ".webp",
                width,height,null, true);
        if(mCache.get(url)==null) {
            mCache.put(url, file_bitmap);
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG,"runOnUiThread file bitmap: "+(file_bitmap==null));
                        View imageView = parent.findViewWithTag(url);
                        Log.d("logtest2","find tag: "+(imageView == null));
                        if(imageView!=null) {
                            if(imageView instanceof ImageView) {
                                ((ImageView)imageView).setImageBitmap(file_bitmap);
                            } else if(imageView instanceof BackgroundView) {
                                ((BackgroundView)imageView).setBitmap(file_bitmap);
                            }
                        }
                    }
                });
            }
        },50);
    }

    /**
     * method for get remote image by http if local no exit
     * @param url remote image url
     * **/
    private void getRemoteImage(final String url,final View parent,final int width, final int height,final boolean corner){
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
                            //byte[] img_bytes = response.body().bytes();
                            InputStream imgStream = response.body().byteStream();
                            String path = "/storage/emulated/0/my_app_cache/"+FileUtils.ParseMd5(url);
                            FileUtils.saveFile(path,imgStream);
                            Bitmap bitmap = BitmapUtils.createScaleBitmapFromFile(path, width, height,null, true);
                            boolean result = FileUtils.deleteFile(path);
                            Log.d(TAG,"delete origin file: "+result);
                            //Bitmap bitmap = BitmapUtils.createScaleFromBytes(img_bytes, width, height,null, true);
                            response.body().close();
                            final Bitmap imageBitmap = corner ? BitmapUtils.CornerBitmapUtils.getCornerBitmap(
                                    bitmap,getResources().getDimensionPixelOffset(R.dimen.app_rect_corner),
                                    BitmapUtils.CornerBitmapUtils.CORNER_ALL)
                                    : bitmap;

                            BitmapUtils.decodeBitmapToFile(imageBitmap,
                                    path,BitmapUtils.TYPE_WEBP);
                            if(mCache.get(url)==null) {
                                mCache.put(url, imageBitmap);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG,"runOnUiThread: "+(imageBitmap==null));
                                    View imageView = parent.findViewWithTag(url);
                                    if(imageView!=null) {
                                        if (imageView instanceof ImageView) {
                                            ((ImageView) imageView).setImageBitmap(imageBitmap);
                                        } else if (imageView instanceof BackgroundView) {
                                            ((BackgroundView) imageView).setBitmap(imageBitmap);
                                        }
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

    protected class NetAdapter extends BaseAdapter {

        private Context mContext;
        private List<AppInfoData> mList;
        private View.OnClickListener mOnClickListener;
        private PackageManager mPackageManager;
        private ListView mListView;
        private int iconSize;

        public NetAdapter (Context context, List<AppInfoData> list, ListView listview) {
            mContext = context;
            mList = list;
            mListView = listview;
            mPackageManager = (PackageManager)mContext.getApplicationContext()
                    .getPackageManager();
            iconSize = getResources().getDimensionPixelOffset(R.dimen.item_icon_size);
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
        public AppInfoData getItem(int position) {
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
            NetAdapter.ViewHolder mHolder = null;
            if(convertView==null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item,null);
                mHolder = new NetAdapter.ViewHolder();
                mHolder.mTitle = convertView.findViewById(R.id.title);
                mHolder.mIcon = convertView.findViewById(R.id.icon);
                mHolder.mSummary = convertView.findViewById(R.id.summary);
                mHolder.mVersion = convertView.findViewById(R.id.version);
                mHolder.mProgress = convertView.findViewById(R.id.progress_btn);
                convertView.setTag(mHolder);
            } else {
                mHolder = (NetAdapter.ViewHolder)convertView.getTag();
            }

            AppInfoData data = (AppInfoData) getItem(position);
            if(data != null) {
                mHolder.mTitle.setText(data.getName());
                mHolder.mSummary.setText(data.getDescri());
                mHolder.mVersion.setText(data.getVersion());

                boolean isInstalled = Utils.isAppInstalled(mPackageManager,data.getPackageName(),
                        data.getVersion());
                boolean isApkExist = FileUtils.isApkExist(getApplicationContext(),data.getAppUrl());
                mHolder.mProgress.setCurrentProgress(isInstalled?101:(isApkExist?100:-1), true);
                mHolder.mProgress.setTag(data.getAppUrl());
                if(isInstalled) {
                    mHolder.mProgress.setOnClickListener(null);
                } else {
                    mHolder.mProgress.setOnClickListener(mOnClickListener);
                }

                String url = data.getUrl();
                mHolder.mIcon.setTag(url);
                if (mCache.get(url) == null) {
                    loadRemoteOrLocalImage(url,mListView,iconSize,iconSize);
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

    static class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }
}
