package com.ice.bitmaputils.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.ice.bitmaputils.R;
import com.ice.bitmaputils.utils.BitmapUtils;

/**
 * Created by walkersky on 2018/3/27.
 */

public class BackgroundView extends View {

    private Paint mPaint;
    private LinearGradient mbgGradient;
    private int mBeginColor;
    private int mEndColor;
    private int mRectSize;
    private int mCorner = 0;
    private Bitmap mBitmap;
    private boolean isRect = false;
    private boolean autoRecycle = false;

    private Path mPath;

    public BackgroundView(Context context) {
        super(context);
    }

    public BackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public BackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /**
     * init customer background view some values
     * @param context Context of this view
     * @param attributeSet AttributeSet
     * **/
    private void init(Context context, AttributeSet attributeSet) {
        TypedArray attrs = context.obtainStyledAttributes(attributeSet, R.styleable.backgroundview);
        try {
            mBeginColor = attrs.getColor(R.styleable.backgroundview_beginColor,0xffff879d);
            mEndColor = attrs.getColor(R.styleable.backgroundview_endColor, 0xffffbf8b);
            isRect = attrs.getBoolean(R.styleable.backgroundview_isRect, false);
            autoRecycle = attrs.getBoolean(R.styleable.backgroundview_autoRecycle,false);
            mCorner = attrs.getDimensionPixelOffset(R.styleable.backgroundview_corner, 0);
            mRectSize = attrs.getDimensionPixelOffset(R.styleable.backgroundview_rectSize, 0);
            Drawable mDrawable = attrs.getDrawable(R.styleable.backgroundview_foreBitmap);
            if(isRect && mDrawable!=null) {
                mBitmap = BitmapUtils.CornerBitmapUtils.getCornerBitmap(((BitmapDrawable)mDrawable).getBitmap().copy(Bitmap.Config.ARGB_8888,true),
                        mCorner, BitmapUtils.CornerBitmapUtils.CORNER_ALL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(attrs!=null) {
                attrs.recycle();
            }
        }
        if(mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
        }
    }

    /**
     * init path to draw decided by isRect
     * **/
    private void initPath(){
        if(mPath == null) {
            mPath = new Path();
            if (!isRect) {
                mPath.moveTo(0,0);
                mPath.lineTo(0,getHeight());
                mPath.lineTo(getWidth(),getHeight()*0.6f);
                mPath.lineTo(getWidth(),0);
                mPath.close();
            } else {
                float left = (getWidth()-mRectSize)*0.5f;
                RectF rectF = new RectF(left,0,left+mRectSize,getHeight());
                mPath.addRoundRect(rectF,mCorner,mCorner, Path.Direction.CCW);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mbgGradient==null) {
            if (!isRect) {
                mbgGradient = new LinearGradient(0,getHeight(),getWidth(),0,
                        mBeginColor,mEndColor, Shader.TileMode.CLAMP);
            } else {
                mbgGradient = new LinearGradient(0,0,getWidth(),0,
                        mBeginColor,mEndColor, Shader.TileMode.CLAMP);
            }
//            mPaint.setShader(mbgGradient);
        }

//        initPath();
//        canvas.drawPath(mPath,mPaint);

        if(isRect && mBitmap!=null && !mBitmap.isRecycled()) {
            int left = (int)((getWidth()-mRectSize)*0.5f);
            canvas.drawBitmap(mBitmap,left,0,mPaint);
        } else {
            mPaint.setShader(mbgGradient);
            initPath();
            canvas.drawPath(mPath,mPaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(autoRecycle && mBitmap!=null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        postInvalidate();
    }
}
