package com.ice.bitmaputils.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.ice.bitmaputils.R;
import com.ice.bitmaputils.utils.BitmapUtils;

/**
 * Created by minwalker on 2018/2/10.
 */

public class BitmapUtilsAcivity extends Activity {

    private final static String TAG = "BitmapUtilsAcivity";
    private ImageView mOriginView, mSampleView, mScaleView, mUtilsView;
    private TextView mOriginTitle, mSampleTitle, mScaleTitle, mUtilsTitle;
    private Bitmap mOriginBitmap, mSampleBitmap, mScaleBitmap, mUtilsBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmap_layout);

        final int bitmap_size = getResources().getDimensionPixelSize(R.dimen.img_size);

        mOriginView = findViewById(R.id.origin_img);
        mSampleView = findViewById(R.id.sample_img);
        mScaleView = findViewById(R.id.scale_img);
        mUtilsView = findViewById(R.id.utils_img);

        mOriginTitle = findViewById(R.id.origin_title);
        mSampleTitle = findViewById(R.id.sample_title);
        mScaleTitle = findViewById(R.id.scale_title);
        mUtilsTitle = findViewById(R.id.utils_title);

        long begin_time = System.currentTimeMillis();
        mOriginBitmap = getmOriginBitmap(R.drawable.test_decode);
        begin_time = (System.currentTimeMillis() - begin_time);
        mOriginView.setImageBitmap(mOriginBitmap);
        mOriginTitle.setText((mOriginBitmap!=null&&!mOriginBitmap.isRecycled() ? "origin bitmap "+formatbitToKB(mOriginBitmap.getByteCount()):null)
                +"\nused time: "+(begin_time)+" ms");

        begin_time = System.currentTimeMillis();
        mSampleBitmap = getSampleBitmap(R.drawable.test_decode,bitmap_size,bitmap_size);
        begin_time = (System.currentTimeMillis() - begin_time);
        mSampleView.setImageBitmap(mSampleBitmap);
        mSampleTitle.setText((mSampleBitmap!=null&&!mSampleBitmap.isRecycled() ? "sample bitmap "+formatbitToKB(mSampleBitmap.getByteCount()):null)
                +"\nused time: "+(begin_time)+" ms");

        begin_time = System.currentTimeMillis();
        mScaleBitmap = Bitmap.createScaledBitmap(getmOriginBitmap(R.drawable.test_decode),bitmap_size,bitmap_size,true);
        begin_time = (System.currentTimeMillis() - begin_time);
        mScaleView.setImageBitmap(mScaleBitmap);
        mScaleTitle.setText((mScaleBitmap!=null&&!mScaleBitmap.isRecycled() ? "scale bitmap: "+formatbitToKB(mScaleBitmap.getByteCount()):null)
                +"\nused time: "+(begin_time)+" ms");

        begin_time = System.currentTimeMillis();
        mUtilsBitmap = BitmapUtils.createScaleBitmapFromRes(this,R.drawable.test_decode,bitmap_size,bitmap_size,null);
        begin_time = (System.currentTimeMillis() - begin_time);
        mUtilsView.setImageBitmap(mUtilsBitmap);
        mUtilsTitle.setText((mUtilsBitmap!=null&&!mUtilsBitmap.isRecycled() ? "utils bitmap "+formatbitToKB(mUtilsBitmap.getByteCount()):null)
                +"\nused time: "+(begin_time)+" ms");


    }

    @Override
    protected void onDestroy() {
        if(mOriginBitmap!=null && !mOriginBitmap.isRecycled()) {
            mOriginBitmap.recycle();
        }

        if(mSampleBitmap!=null && !mSampleBitmap.isRecycled()) {
            mSampleBitmap.recycle();
        }

        if(mUtilsBitmap!=null && !mUtilsBitmap.isRecycled()) {
            mUtilsBitmap.recycle();
        }

        if(mScaleBitmap!=null && !mScaleBitmap.isRecycled()) {
            mScaleBitmap.recycle();
        }
        super.onDestroy();
    }

    private Bitmap getSampleBitmap(int resId, int width, int height){
        if(resId<=0 || width <0 || height<0) {
            return null;
        }

        Bitmap result = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            result = BitmapFactory.decodeResource(getResources(),resId,options);
            Log.d(TAG,"get origin bitmap width: "+options.outWidth+" height: "+options.outHeight);
            int maxSize = Math.max(options.outWidth, options.outHeight);
            int maxTargetSize = Math.max(width, height);
            if(maxSize <= maxTargetSize) {
                options.inSampleSize = 1;
            } else {
                options.inSampleSize = maxSize / maxTargetSize;
            }

            options.inJustDecodeBounds = false;
            result = BitmapFactory.decodeResource(getResources(),resId,options);
            Log.d(TAG,"get deocde sample size: "+options.inSampleSize);
            Log.d(TAG, "get deocde result width: "+result.getWidth()+" : "+result.getHeight());
            Log.d(TAG,"get target image width: "+width+" height: "+height);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private Bitmap getmOriginBitmap(int resId){
        if(resId <= 0) {
            return null;
        }

        Bitmap result = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            result = BitmapFactory.decodeResource(getResources(),resId,options);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private String formatbitToKB(int bytesize) {
        return "size: "+String.valueOf(bytesize*1.0f/1024)+"KB";
    }
}
