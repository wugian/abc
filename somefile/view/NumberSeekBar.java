
package com.xgimi.gimicinema.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;
import com.xgimi.gimicinema.R;
import com.xgimi.gimicinema.utils.Tools;

/**
 * @类名: NumberSeekBar
 * @描述: (带有数字的水平拖动条)
 * @作者: wang.fb
 * @日期: 2014-8-11 下午2:01:14
 * @修改人:
 * @修改时间: 2014-8-11 下午2:01:14
 * @修改内容:
 * @版本: V1.0
 * @版权:Copyright © 2014 云盛海宏信息技术（深圳）有限公司 . All rights reserved.
 */
public class NumberSeekBar extends SeekBar {

    private int oldPaddingTop;

    private int oldPaddingLeft;

    private int oldPaddingRight;

    private int oldPaddingBottom;

    private boolean isMysetPadding = true;

    private String mText;

    private float mTextWidth;

    private float mImgWidth;

    private float mImgHei;

    private Paint mPaint;

    private Resources res;

    private Bitmap bm;

    private int textSize = 18;

    private int textPaddingLeft;

    private int textPaddingTop;

    private int imagePaddingLeft;

    private int imagePaddingTop;
    private int py = -5;

    public NumberSeekBar(Context context) {
        super(context);
        init();
    }

    public NumberSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NumberSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    // 屏蔽滑动
    // @Override
    // public boolean onTouchEvent(MotionEvent event) {
    // return false;
    // }

    /**
     * (非 Javadoc)
     *
     * @param event
     * @return
     * @方法名: onTouchEvent
     * @描述: 不屏蔽屏蔽滑动
     * @日期: 2014-8-11 下午2:03:15
     * @see android.widget.AbsSeekBar#onTouchEvent(MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    // 修改setpadding 使其在外部调用的时候无效
    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        if (isMysetPadding) {
            super.setPadding(left, top, right, bottom);
        }
    }

    // 初始化
    private void init() {
        res = getResources();
        initBitmap();
        initDraw();
        setPadding();
    }

    private void initDraw() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTypeface(Typeface.DEFAULT);
        mPaint.setTextSize(textSize);
        mPaint.setColor(0xffffffff);
    }

    private void initBitmap() {
        bm = BitmapFactory.decodeResource(res, R.drawable.ic_time_bg);
        if (bm != null) {
            mImgWidth = bm.getWidth();
            mImgHei = bm.getHeight();
        } else {
            mImgWidth = 0;
            mImgHei = 0;
        }
    }

    private boolean bmVisible = false;

    public void setProcessVisible(boolean bmVisible) {
        this.bmVisible = bmVisible;
    }

    public boolean processVisible() {
        return bmVisible;
    }

    protected synchronized void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
            int secondaryProgress = getSecondaryProgress()/* == 0 ? getProgress() : getSecondaryProgress()*/;
            mText = Tools.formatDuration(getSecondaryProgress());
            //(getProgress() * 100 / getMax()) + "%";
            mTextWidth = mPaint.measureText(mText);
            Rect bounds = this.getProgressDrawable().getBounds();
            float i = (float) ((double) secondaryProgress / (double) getMax());
            float xImg =
                    bounds.width() * i + imagePaddingLeft
                            + oldPaddingLeft;
            float yImg = imagePaddingTop + oldPaddingTop;
            float xText =
                    bounds.width() * i + mImgWidth / 2
                            - mTextWidth / 2 + textPaddingLeft + oldPaddingLeft;
            float yText =
                    yImg + textPaddingTop + mImgHei / 2 + getTextHei() / 8;
            if (bmVisible) {
                canvas.drawBitmap(bm, xImg, yImg, mPaint);
                canvas.drawText(mText, xText, yText, mPaint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 初始化padding 使其左右上 留下位置用于展示进度图片
    private void setPadding() {
        int top = getBitmapHeigh() + oldPaddingTop;
        int left = getBitmapWidth() / 2 + oldPaddingLeft;
        int right = getBitmapWidth() / 2 + oldPaddingRight;
        isMysetPadding = true;
        setPadding(left, top, right, 14);
        isMysetPadding = false;
    }

    /**
     * 设置展示进度背景图片
     *
     * @param resid
     */
    public void setBitmap(int resid) {
        bm = BitmapFactory.decodeResource(res, resid);
        if (bm != null) {
            mImgWidth = bm.getWidth();
            mImgHei = bm.getHeight();
        } else {
            mImgWidth = 0;
            mImgHei = 0;
        }
        setPadding();
    }

    /**
     * 替代setpadding
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setMyPadding(int left, int top, int right, int bottom) {
        oldPaddingTop = top;
        oldPaddingLeft = left;
        oldPaddingRight = right;
        oldPaddingBottom = bottom;
        isMysetPadding = true;
        setPadding(left + getBitmapWidth() / 2, top + getBitmapHeigh(), right
                + getBitmapWidth() / 2, bottom);
        isMysetPadding = false;
    }

    /**
     * 设置进度字体大小
     *
     * @param textsize
     */
    public void setTextSize(int textsize) {
        this.textSize = textsize;
        mPaint.setTextSize(textsize);
    }

    /**
     * 设置进度字体颜色
     *
     * @param color
     */
    public void setTextColor(int color) {
        mPaint.setColor(color);
    }

    /**
     * 调整进度字体的位置 初始位置为图片的正中央
     *
     * @param top
     * @param left
     */
    public void setTextPadding(int top, int left) {
        this.textPaddingLeft = left;
        this.textPaddingTop = top;
    }

    /**
     * 调整进图背景图的位置 初始位置为进度条正上方、偏左一半
     *
     * @param top
     * @param left
     */
    public void setImagePadding(int top, int left) {
        this.imagePaddingLeft = left;
        this.imagePaddingTop = top;
    }

    private int getBitmapWidth() {
        return (int) Math.ceil(mImgWidth);
    }

    private int getBitmapHeigh() {
        return (int) Math.ceil(mImgHei);
    }

    private float getTextHei() {
        FontMetrics fm = mPaint.getFontMetrics();
        return (float) Math.ceil(fm.descent - fm.top) + 2;
    }

    public int getTextPaddingLeft() {
        return textPaddingLeft;
    }

    public int getTextPaddingTop() {
        return textPaddingTop;
    }

    public int getImagePaddingLeft() {
        return imagePaddingLeft;
    }

    public int getImagePaddingTop() {
        return imagePaddingTop;
    }

    public int getTextSize() {
        return textSize;
    }

}
