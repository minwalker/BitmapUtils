package com.ice.bitmaputils.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ice.bitmaputils.BuildConfig;
import com.ice.bitmaputils.R;
import com.ice.bitmaputils.data.AppInfoData;
import com.ice.bitmaputils.services.HttpService;
import com.ice.bitmaputils.utils.FileUtils;
import com.ice.bitmaputils.utils.Utils;
import com.ice.bitmaputils.view.BackgroundView;
import com.ice.bitmaputils.view.ProgressButton;

import java.util.List;

/**
 * Created by minwalker on 2018/4/10.
 */

public class AppInfoActivity extends BaseHttpActivity implements View.OnClickListener{

    private RecyclerView mInfoPager;
    private TextView mTitle,mInfo,mAppSize,mAppRate;
    private ImageView mIcon, mBack;
    private RatingBar mAppRateBar;
    private ProgressButton mProgress;
    private View mTopLayout;
    private AppInfoData mData;
    private HandlerThread mImageThread;
    private Handler mImageHandler;
    private BitmapFactory.Options mOption;

    private final int MSG_LOAD_TOP = 1;
    private final int MSG_LOAD_PAGER = 2;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String app_url = intent.getStringExtra("app_url");
                if(mProgress!=null && mData!=null && app_url.equals(mData.getAppUrl())) {
                    int progress = intent.getIntExtra("progress",0);
                    mProgress.setCurrentProgress(progress,false);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info_layout);
        mData = (AppInfoData) getIntent().getSerializableExtra("app_info");
        mOption = new BitmapFactory.Options();
        mOption.inPreferredConfig = BuildConfig.HIGH_ARGB
                ?Bitmap.Config.ARGB_8888:Bitmap.Config.ARGB_4444;
        initView();
        initThread();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(AppInfoActivity.this,
                LinearLayoutManager.HORIZONTAL,false);
        mInfoPager.setLayoutManager(mLayoutManager);
        mInfoPager.setAdapter(new CardAdapter(mData.getImageList()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mReceiver!=null) {
            IntentFilter mfilter = new IntentFilter();
            mfilter.addAction("com.http.progress.refresh");
            registerReceiver(mReceiver,mfilter);
        }

        refreshProgress();
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
        if(mImageThread!=null) {
            mImageThread.quit();
        }
        super.onDestroy();
    }

    private void initView() {
        mInfoPager = findViewById(R.id.app_info_pager);
        mTitle = findViewById(R.id.app_title);
        mInfo = findViewById(R.id.app_info);
        mIcon = findViewById(R.id.app_icon);
        mBack = findViewById(R.id.back_icon);
        mTopLayout = findViewById(R.id.top_layout);
        mProgress = findViewById(R.id.progress_btn);
        mAppSize = findViewById(R.id.app_size);
        mAppRate = findViewById(R.id.rate_size);
        mAppRateBar = findViewById(R.id.app_rating);

        mIcon.setTag(mData.getUrl());
        mTitle.setText(mData.getName());
        mInfo.setText(mData.getDescri());
        mAppSize.setText(mData.getAppSize());
        mAppRate.setText(String.valueOf(mData.getAppRate()));
        mAppRateBar.setRating(mData.getAppRate());

        mBack.setOnClickListener(this);
    }

    private void refreshProgress() {
        boolean isInstalled = Utils.isAppInstalled(getPackageManager(),mData.getPackageName(),
                mData.getVersion());
        boolean isApkExist = FileUtils.isApkExist(getApplicationContext(),mData.getAppUrl());
        mProgress.setCurrentProgress(isInstalled?101:(isApkExist?100:-1), true);
        if(isInstalled) {
            mProgress.setOnClickListener(null);
        } else {
            mProgress.setOnClickListener(this);
        }
    }

    private void initThread() {
        if(mImageThread == null) {
            mImageThread = new HandlerThread("http_image_thread");
            mImageThread.start();
        }

        if(mImageHandler == null) {
            mImageHandler = new Handler(mImageThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MSG_LOAD_TOP:
                            int size = getResources().getDimensionPixelOffset(R.dimen.item_icon_size);
                            loadRemoteOrLocalImage(mData.getUrl(),mTopLayout,size,size);
                            break;
                        case MSG_LOAD_PAGER:
                            int width = getResources().getDimensionPixelOffset(R.dimen.app_info_img_width);
                            int height = getResources().getDimensionPixelOffset(R.dimen.app_info_card_height);
                            loadRemoteOrLocalImage((String) msg.obj, mInfoPager,
                                    width, height,true, mOption);
                            break;
                    }
                }
            };
        }

        mImageHandler.sendEmptyMessage(MSG_LOAD_TOP);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.progress_btn:
                String url = mData.getAppUrl();
                if(!TextUtils.isEmpty(url)) {
                    Intent intent = new Intent(getApplicationContext(), HttpService.class);
                    intent.putExtra("app_url", url);
                    startService(intent);
                }
                break;
            case R.id.back_icon:
                onBackPressed();
                break;
        }
    }


    public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ItemViewHolder> {
        private List mDatas;
        public CardAdapter(List datas) {
            super();
            mDatas = datas;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent,false);
            return new ItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            holder.mInfoView.setTag(mDatas.get(position));
        }

        @Override
        public void onViewAttachedToWindow(ItemViewHolder holder) {
            if(mImageThread!=null && mImageHandler!=null && holder.mInfoView.getTag()!=null) {
                Message msg = new Message();
                msg.what = MSG_LOAD_PAGER;
                msg.obj = holder.mInfoView.getTag();
                mImageHandler.sendMessage(msg);
            }
            super.onViewAttachedToWindow(holder);
        }

        @Override
        public void onViewRecycled(ItemViewHolder holder) {
            super.onViewRecycled(holder);
        }

        @Override
        public int getItemCount() {
            if(mDatas!=null) {
                return mDatas.size();
            }
            return 0;
        }

        class ItemViewHolder extends RecyclerView.ViewHolder{
            private BackgroundView mInfoView;
            public ItemViewHolder(View itemView) {
                super(itemView);
                mInfoView = itemView.findViewById(R.id.card_icon);
            }
        }
    }

}
