package com.ice.bitmaputils.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ice.bitmaputils.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mEnterBitmapBtn, mEnterCornerBtn, mEnterSaveBtn;
    private Button mEnterHttpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }

        mEnterBitmapBtn = (Button) findViewById(R.id.enter_bitmap_btn);
        mEnterCornerBtn = (Button) findViewById(R.id.enter_corner_btn);
        mEnterSaveBtn = (Button) findViewById(R.id.enter_save_btn);
        mEnterHttpBtn = (Button) findViewById(R.id.enter_http_btn);

        mEnterBitmapBtn.setOnClickListener(this);
        mEnterCornerBtn.setOnClickListener(this);
        mEnterSaveBtn.setOnClickListener(this);
        mEnterHttpBtn.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100) {
            if(permissions[0]!=null && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.enter_bitmap_btn:
                intent = new Intent(MainActivity.this, BitmapUtilsAcivity.class);
                break;
            case R.id.enter_corner_btn:
                intent = new Intent(MainActivity.this, CornerBitmapActivity.class);
                break;
            case R.id.enter_save_btn:
                intent = new Intent(MainActivity.this, SaveBitmapActivity.class);
                break;
            case R.id.enter_http_btn:
                intent = new Intent(MainActivity.this, HttpActivity.class);
                break;
        }

        if(intent!=null) {
            startActivity(intent);
        }
    }
}
