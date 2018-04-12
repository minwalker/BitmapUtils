package com.ice.bitmaputils.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ice.bitmaputils.R;
import com.ice.bitmaputils.data.AppInfoData;
import com.ice.bitmaputils.view.ProgressButton;

import java.util.ArrayList;

/**
 * Created by walkersky on 2018/4/11.
 */

public class AppListActivity extends BaseHttpActivity implements AdapterView.OnItemClickListener{
    private ListView mListView;
    private NetAdapter mAdapter;
    private LoadAsyncTask mTask;

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
        setContentView(R.layout.activity_app_list_layout);
        ArrayList<AppInfoData> mListDatas = (ArrayList<AppInfoData>) getIntent()
                .getSerializableExtra("apps_list");
        mListView = findViewById(R.id.list_view);
        mListView.setOnItemClickListener(AppListActivity.this);
        mTask = new LoadAsyncTask(mListDatas);
        mTask.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        AppInfoData data = mAdapter.getItem(position);
        if(data!=null) {
            Intent intent = new Intent(AppListActivity.this, AppInfoActivity.class);
            intent.putExtra("app_info",data);
            startActivity(intent);
        }
    }

    private void initList(ArrayList<AppInfoData> datas){
        int type = getIntent().getIntExtra("app_type",1);
        switch (type) {
            case R.id.type_top:
                type = 1;
                break;
            case R.id.type_video:
                type = 2;
                break;
            case R.id.type_game:
                type = 3;
                break;
            case R.id.type_camera:
                type = 4;
                break;
        }

        int length = datas.size();
        ArrayList<AppInfoData> result = new ArrayList<AppInfoData>();
        for (int i=0;i<length;i++) {
            AppInfoData data = datas.get(i);
            if(data.getAppTyep() != type) {
                result.add(data);
            }
        }

        datas.removeAll(result);
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
        if(mTask!=null && !mTask.isCancelled()) {
            mTask.cancel(true);
        }
    }

    class LoadAsyncTask extends AsyncTask<ArrayList<AppInfoData>, Void, ArrayList<AppInfoData>>{

        private ArrayList<AppInfoData> mList;

        public LoadAsyncTask(ArrayList<AppInfoData> list) {
            mList = list;
        }

        @Override
        protected ArrayList<AppInfoData> doInBackground(ArrayList<AppInfoData>[] objects) {
            initList(mList);
            return mList;
        }

        @Override
        protected void onPostExecute(ArrayList<AppInfoData> result) {
            mAdapter = new NetAdapter(getApplicationContext(), result, mListView);
            mListView.setAdapter(mAdapter);
        }
    }

}
