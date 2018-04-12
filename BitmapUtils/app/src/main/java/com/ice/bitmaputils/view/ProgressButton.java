package com.ice.bitmaputils.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.ice.bitmaputils.R;

/**
 * Created by minwalker on 2018/3/26.
 */

public class ProgressButton extends View {

    private int mProgress = 0;
    private int mMaxProgress;
    private int mPaddingX;
    private int mPaddingY;

    private float mRadius;

    private int mWidth = -1;
    private int mHeight = -1;
    private int centerY;
    private int mDividerWidth;

    private int mForeGroundColor;
    private int mBackGroundColor;
    private int mTextColor;

    private RectF mBackRect;
    private RectF mForeRect;

    private String mDownLoadText;
    private String mCompleteText;
    private String mInstalledText;
    private String mCancelText;

    private Paint mPaint;
    private Paint mTextPaint;

    private PorterDuffXfermode mXferMode;
    private PorterDuffXfermode mClearMode;

    public ProgressButton(Context context) {
        super(context);
    }

    public ProgressButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ProgressButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ProgressButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /**
     * init all objecct for this view, mPaint is the background paint,
     * mTextPaint is for text drawing
     * @param context Context of this View
     * @param attributeSet AttributeSet
     * **/
    private void init(Context context, AttributeSet attributeSet) {
        TypedArray attrs = context.obtainStyledAttributes(attributeSet, R.styleable.progressbutton);
        int mTextSize = 18;
        float mTextSpace = 0.08f;
        try {
            mMaxProgress = attrs.getInt(R.styleable.progressbutton_maxProgress,100);

            mRadius = attrs.getFloat(R.styleable.progressbutton_buttonCornerRadius, 5.0f);
            mPaddingX = attrs.getDimensionPixelOffset(R.styleable.progressbutton_padding_x, 8);
            mPaddingY = attrs.getDimensionPixelOffset(R.styleable.progressbutton_padding_y, 4);

            mForeGroundColor = attrs.getColor(R.styleable.progressbutton_foreGroundColor,0x00000000);
            mBackGroundColor = attrs.getColor(R.styleable.progressbutton_backGroundColor, 0x00ffffff);
            mTextColor = attrs.getColor(R.styleable.progressbutton_perTextColor,0x00000000);

            mDownLoadText = attrs.getString(R.styleable.progressbutton_download_text);
            mCompleteText = attrs.getString(R.styleable.progressbutton_complete_text);
            mCancelText = attrs.getString(R.styleable.progressbutton_cancle_text);
            mInstalledText = attrs.getString(R.styleable.progressbutton_installed_text);

            mTextSize = attrs.getInt(R.styleable.progressbutton_text_size, 18);
            mTextSpace = attrs.getFloat(R.styleable.progressbutton_text_spacing,0.08f);

            mDividerWidth = attrs.getDimensionPixelSize(R.styleable.progressbutton_divider_width,2);
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
            mXferMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
            mClearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
            mProgress = -1;
        }

        if(mTextPaint == null) {
            mTextPaint = new Paint();
            mTextPaint.setAntiAlias(true);
            mTextPaint.setTextSize(mTextSize);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setLetterSpacing(mTextSpace);
            mTextPaint.setColor(mTextColor);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mPaint!=null) {
            initRect();
            drawProgress(canvas);
            drawText(canvas);
            drawButton(canvas);
        }
    }

    /**
     * only init once the rect of the button areas,
     * and get the centerY to draw text.
     **/
    private void initRect(){
        if(mBackRect == null) {
            if(mWidth == -1) {
                mWidth = getWidth();
            }

            if(mHeight == -1) {
                mHeight = getHeight();
            }

            Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
            centerY = mHeight - fontMetrics.bottom - fontMetrics.top;
            mBackRect = new RectF(mPaddingX,mPaddingY,mWidth-mPaddingX,mHeight-mPaddingY);
            mForeRect = new RectF(mPaddingX,mPaddingY,mWidth-mPaddingX,mHeight-mPaddingY);
        }
    }

    /**
     * draw progress forground
     * @param canvas Canvas of this view
     * **/
    private void drawProgress(Canvas canvas) {
        int progress = mProgress > 100?100:mProgress;
        float right = (mWidth-mPaddingX)*(progress*1.0f/mMaxProgress);
        if(right > mPaddingX) {
            mForeRect.set(mPaddingX, mPaddingY, right, mHeight - mPaddingY);

            int save = canvas.saveLayer(mBackRect, null);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mBackGroundColor);
            canvas.drawRoundRect(mBackRect, mRadius, mRadius, mPaint);

            mPaint.setColor(mForeGroundColor);
            mPaint.setXfermode(mXferMode);
            canvas.drawRect(mForeRect,mPaint);

            mPaint.setXfermode(mClearMode);
            mForeRect.set(right, mPaddingY, mWidth-mPaddingX, mHeight - mPaddingY);
            canvas.drawRect(mForeRect,mPaint);
            mPaint.setXfermode(null);
            canvas.restoreToCount(save);
        }
    }

    /**
     * draw button text decided by current progress
     * @param canvas Canvas of this view
     * **/
    private void drawText(Canvas canvas) {
        if(mProgress < 0) {
            mTextPaint.setColor(mTextColor);
            canvas.drawText(mDownLoadText, mWidth * 0.5f, centerY * 0.5f, mTextPaint);
        } else if(mProgress == 100) {
            mTextPaint.setColor(Color.WHITE);
            canvas.drawText(mCompleteText, mWidth * 0.5f, centerY * 0.5f, mTextPaint);
        } else if(mProgress > 100){
            mTextPaint.setColor(Color.WHITE);
            canvas.drawText(mInstalledText, mWidth * 0.5f, centerY * 0.5f, mTextPaint);
        } else {
            mTextPaint.setColor(mTextColor);
            float right = (mWidth-mPaddingX)*(mProgress*1.0f/mMaxProgress);
            canvas.drawText(getResources().getString(R.string.progress_btn_percent,mProgress), mWidth * 0.5f, centerY * 0.5f, mTextPaint);
            if(right > mPaddingX) {
                mForeRect.set(mPaddingX, mPaddingY, right, mHeight - mPaddingY);
                int save = canvas.saveLayer(mBackRect, null);
                canvas.drawText(getResources().getString(R.string.progress_btn_percent,mProgress), mWidth * 0.5f, centerY * 0.5f, mTextPaint);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(Color.WHITE);
                mPaint.setXfermode(mXferMode);
                canvas.drawRect(mForeRect,mPaint);
                mPaint.setXfermode(null);
                canvas.restoreToCount(save);
            }
        }
    }

    /**
     * draw button background
     * @param canvas Canvas of this view
     * **/
    private void drawButton(Canvas canvas){
        mPaint.setColor(mBackGroundColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mDividerWidth);
        canvas.drawRoundRect(mBackRect, mRadius, mRadius, mPaint);
    }

    /**
     * method for change the current progress
     * and draw it
     * @param progress current progress
     * **/
    public void setCurrentProgress(int progress,boolean init){
        if(mProgress!=progress) {
            mProgress = progress;
            if(!init) {
                postInvalidate();
            }
        }
    }
}
