package com.ice.bitmaputils.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import com.ice.bitmaputils.R;
import com.ice.bitmaputils.activity.MainActivity;
import com.ice.bitmaputils.utils.FileUtils;
import com.ice.bitmaputils.utils.ProgressResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by minwalker on 2018/3/10.
 */

public class HttpService extends IntentService {

    private static final String TAG = "HttpService";
    private OkHttpClient mClient;
    private Call mCall;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private String current_url;
    private int curProgress = 0;

    public HttpService(String name) {
        super("http_service");
    }

    public HttpService(){
        super("http_service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final String app_url = intent.getStringExtra(
                getApplication().getResources().getString(R.string.download_service_url_key));
        current_url = app_url;
        Log.d(TAG, "on http service handleIntent: "+app_url);
        if(!TextUtils.isEmpty(app_url)) {
            downloadOrInstall(app_url);
        }

    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        try {
            if (mCall != null && !mCall.isCanceled()) {
                mCall.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * always make sure then our OkHttpClient is not Null
     * and init some Listener and Operation of it
     * **/
    private void initClient() {
        if(mClient== null) {

            final ProgressResponseBody.ProgressListener mProgressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    int progress = (int)(100*(bytesRead*1.0f/contentLength));
                    if(mNotificationManager!=null) {
                        updateProgress(progress);
                        if(curProgress == 100) {
                            CancleNotification();
                        }
                    }
                }
            };
            Interceptor interceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(),mProgressListener))
                            .build();
                }
            };

            mClient = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .addNetworkInterceptor(interceptor)
                    .build();
        }
    }

    /**
     * make sure whether the apk file is exit, if exit just start install,
     * else begin our http connect to download it first
     * @param app_url remote apk file url
     * **/
    private void downloadOrInstall(String app_url){
        try {
            StringBuilder pathBuild = new StringBuilder();
            pathBuild.append(getApplication().getResources().getString(R.string.download_default_path));
            pathBuild.append(app_url.substring(app_url.lastIndexOf('/')+1,app_url.length()));
            String apkPath = pathBuild.toString();
            File file = new File(apkPath);
            if(file.exists()) {
                startInstall(file);
            } else {
                initClient();
                Request request = new Request.Builder()
                        .addHeader("Accept-Encoding", "gzip, deflate")
                        .url(app_url)
                        .build();
                mCall = mClient.newCall(request);
                showNotification();
                Response response = mCall.execute();
                Log.d(TAG,"on httpService response: "+response.isSuccessful()+" : "+response.code());
                if(response.isSuccessful()) {
                    byte[] bytes = response.body().bytes();
                    boolean result = FileUtils.saveApk(apkPath,bytes);
                    response.body().close();
                    if(result) {
                        file = new File(apkPath);
                        startInstall(file);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * start install activity of system to install our apk,
     * after SDK version up 24 need using fileprovider to
     * get the file otherwise will get exception
     * @param file install apk file
     * **/
    private void startInstall(File file){
        try {
            Intent apk_intent = new Intent(Intent.ACTION_VIEW);
            apk_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            apk_intent.addCategory(Intent.CATEGORY_DEFAULT);
            if (Build.VERSION.SDK_INT >= 24) {
                Uri apkUri = FileProvider.getUriForFile(getApplicationContext(), "com.ice.bitmaputils.fileprovider", file);
                apk_intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                apk_intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                apk_intent.setDataAndType(Uri.fromFile(file),
                        "application/vnd.android.package-archive");
            }
            getApplication().startActivity(apk_intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * init Notification for file download progress
     * for SystemUI has rencet clear Feature, so we
     * need our service running in foreground otherwise
     * we may be kill
     * **/
    private void showNotification() {
        if(mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if(mBuilder == null) {
            mBuilder = new NotificationCompat.Builder(getApplicationContext(),"http_service");
            Intent intent = new Intent();
            intent.setClass(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);
        }

        mBuilder.setContentTitle(getApplication().getResources().getString(R.string.notif_download_title));
        mBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        mBuilder.setOngoing(true);
        mBuilder.setProgress(100,0,false);
        curProgress = 0;
        startForeground(100, mBuilder.build());
//        mNotificationManager.notify(100,mBuilder.build());
    }


    /**
     * method using update the notification progress, for
     * reduce recycle update add curProgress, make sure
     * the progress can only update 100 times most, otherwise
     * app may crash in update
     * @param progress current http download progress
     * **/
    private void updateProgress(int progress) {
        if(mNotificationManager!=null && mBuilder!=null) {
            if (curProgress < progress) {
                if(progress == 100) {
                    mBuilder.setContentTitle(getApplication().getResources().getString(R.string.notif_download_complete_title));
                }
                mBuilder.setProgress(100, progress, false);
                mNotificationManager.notify(100, mBuilder.build());
                Intent mProgressIntent = new Intent();
                mProgressIntent.setAction("com.http.progress.refresh");
                mProgressIntent.putExtra("app_url",current_url);
                mProgressIntent.putExtra("progress",progress);
                sendBroadcast(mProgressIntent);
                curProgress = progress;
            }
        }
    }

    /**
     * method to dimiss notification
     * **/
    public void CancleNotification() {
        if(mNotificationManager!=null) {
            stopForeground(true);
//            mNotificationManager.cancel(100);
        }
    }
}
