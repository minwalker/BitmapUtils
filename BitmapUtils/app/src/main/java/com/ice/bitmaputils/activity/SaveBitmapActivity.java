package com.ice.bitmaputils.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.TextView;


import com.ice.bitmaputils.R;
import com.ice.bitmaputils.utils.BitmapUtils;

import java.io.File;

/**
 * Created by minwalker on 2018/2/10.
 */

public class SaveBitmapActivity extends Activity {
    private Bitmap mOriginBitmap;
    private ImageView mOrginView;
    private TextView mOriginTitle,mPngTitle,mJpegTitle,mWebpTitle;
    private boolean mPremission = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_layout);
        if(this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            mPremission = false;
        } else {
            mPremission = true;
        }

        final int bitmap_size = getResources().getDimensionPixelSize(R.dimen.img_size);

        mOrginView = findViewById(R.id.origin_img);

        mOriginTitle = findViewById(R.id.origin_title);
        mPngTitle = findViewById(R.id.png_title);
        mJpegTitle = findViewById(R.id.jpeg_title);
        mWebpTitle = findViewById(R.id.webp_title);

        mOriginBitmap = BitmapUtils.createScaleBitmapFromRes(this,R.drawable.test_decode,bitmap_size,bitmap_size,null);

        mOrginView.setImageBitmap(mOriginBitmap);
        mOriginTitle.setText(mOriginBitmap!=null&&!mOriginBitmap.isRecycled()?formatbitToKB(mOriginBitmap.getByteCount()):null);
        if(mPremission) {
            saveBitmapToFile("/storage/emulated/0/test_image/decode_image");
            getFileSizeShow("/storage/emulated/0/test_image/decode_image");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100) {
            if(permissions[0]!=null && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveBitmapToFile("/storage/emulated/0/test_image/decode_image");
                getFileSizeShow("/storage/emulated/0/test_image/decode_image");
                mPremission = true;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void saveBitmapToFile(String path) {
        BitmapUtils.decodeBitmapToFile(mOriginBitmap, path, BitmapUtils.TYPE_PNG);
        BitmapUtils.decodeBitmapToFile(mOriginBitmap, path, BitmapUtils.TYPE_JPEG);
        BitmapUtils.decodeBitmapToFile(mOriginBitmap, path, BitmapUtils.TYPE_WEBP);
    }

    private void getFileSizeShow(String path) {
        mPngTitle.setText("Png type size: "+getFileSize(path+".png"));
        mJpegTitle.setText("Jpeg type size: "+getFileSize(path+".jpg"));
        mWebpTitle.setText("Webp type size: "+getFileSize(path+".webp"));
    }

    @Override
    protected void onDestroy() {
        if(mOriginBitmap!=null && !mOriginBitmap.isRecycled()) {
            mOriginBitmap.recycle();
        }
        super.onDestroy();
    }

    private String formatbitToKB(int bytesize) {
        return "bitmap size: "+String.valueOf(bytesize*1.0f/1024)+"KB";
    }

    private String formatbitToKB(long bytesize) {
        return "bitmap size: "+String.valueOf(bytesize*1.0f/1024)+"KB";
    }

    private String getFileSize(String path){
        File file = new File(path);
        if(file.exists()&&file.isFile()) {
            return formatbitToKB(file.length());
        } else {
            return "fail to get size";
        }
    }
}
