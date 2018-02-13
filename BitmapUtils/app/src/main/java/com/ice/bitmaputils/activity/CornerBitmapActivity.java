package com.ice.bitmaputils.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.ice.bitmaputils.R;
import com.ice.bitmaputils.utils.BitmapUtils;

/**
 * Created by minwalker on 2018/2/10.
 */

public class CornerBitmapActivity extends Activity {

    private ImageView mOriginView,mCornerView,mOvalView,mReflectedView,mBlurView;
    private Bitmap mOriginBitmap, mCornerBitmap, mOvalBitmap,mReflectedBitmap,mBlurBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corner_layout);

        mOriginView = findViewById(R.id.origin_img);
        mCornerView = findViewById(R.id.corner_img);
        mOvalView = findViewById(R.id.oval_img);
        mReflectedView = findViewById(R.id.reflected_img);
        mBlurView = findViewById(R.id.blur_img);

        final int bitmap_size = getResources().getDimensionPixelSize(R.dimen.img_size);

        mOriginBitmap = BitmapUtils.createScaleBitmapFromRes(this,R.drawable.test_decode,bitmap_size,bitmap_size,null);
        mCornerBitmap = BitmapUtils.CornerBitmapUtils.getCornerBitmap(mOriginBitmap.copy(mOriginBitmap.getConfig(),true),10,
                BitmapUtils.CornerBitmapUtils.CORNER_ALL);
        mOvalBitmap = BitmapUtils.CornerBitmapUtils.getOvalBitmap(mOriginBitmap.copy(mOriginBitmap.getConfig(),true));
        mReflectedBitmap = BitmapUtils.CornerBitmapUtils.getReflectedBitmap(getApplication(),mOriginBitmap.copy(mOriginBitmap.getConfig(),true),10);
        mBlurBitmap = BitmapUtils.blurBitmap(this,mOriginBitmap.copy(mOriginBitmap.getConfig(),true),20);

        mOriginView.setImageBitmap(mOriginBitmap);
        mCornerView.setImageBitmap(mCornerBitmap);
        mOvalView.setImageBitmap(mOvalBitmap);
        mReflectedView.setImageBitmap(mReflectedBitmap);
        mBlurView.setImageBitmap(mBlurBitmap);

    }

    @Override
    protected void onDestroy() {

        if(mOriginBitmap!=null && !mOriginBitmap.isRecycled()) {
            mOriginBitmap.recycle();
        }

        if(mCornerBitmap!=null && !mCornerBitmap.isRecycled()) {
            mCornerBitmap.recycle();
        }

        if(mOvalBitmap!=null && !mOvalBitmap.isRecycled()) {
            mOvalBitmap.recycle();
        }

        if(mReflectedBitmap!=null && !mReflectedBitmap.isRecycled()) {
            mReflectedBitmap.recycle();
        }

        if(mBlurBitmap!=null && !mBlurBitmap.isRecycled()) {
            mBlurBitmap.recycle();
        }
        super.onDestroy();
    }
}
