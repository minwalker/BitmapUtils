package com.ice.bitmaputils.activity;

import android.content.Intent;
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
