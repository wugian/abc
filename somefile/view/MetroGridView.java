package com.xgimi.gimicinema.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.GridView;

public class MetroGridView extends GridView {
    private int position = 0;
    private boolean isMySetPadding = false;

    public MetroGridView(Context context) {
        super(context);
        isMySetPadding = true;
//        setPadding(60, 60, 60, 60);
        isMySetPadding = false;
    }

    public MetroGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setChildrenDrawingOrderEnabled(true);
        isMySetPadding = true;
//        setPadding(60, 60, 60, 60);
        isMySetPadding = false;
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        if (isMySetPadding) {
            super.setPadding(left, top, right, bottom);
        }
    }

    public void setCurrentPosition(int pos) {
        this.position = pos;
    }

    @SuppressLint("NewApi")
    @Override
    protected void setChildrenDrawingOrderEnabled(boolean enabled) {
        super.setChildrenDrawingOrderEnabled(enabled);
    }


    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (i == childCount - 1) {
            return position;
        }
        if (i == position) {
            return childCount - 1;
        }
        return i;
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction,
                                  Rect previouslyFocusedRect) {
        int lastSelectItem = getSelectedItemPosition();
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            setSelection(lastSelectItem);
        }
    }

    private final static int SCROLL_ITEM_TIME = 1000;//may not important
    private int eventCount = 0;
    private final static int DOUBLE_ROW = 2; //double row
    private final static int SINGLE_ROW = 1; //single row

    private int scrollRow = 1;//set the scroll rows if needed default is 1

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.getChildCount() > 0) {
            int height = this.getChildAt(0).getHeight();
            eventCount++;
            //for different item it may occurs twice
            if (eventCount % 2 != 0) {
                int row = 0;
                row = getItemCurrentRow();
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && row == DOUBLE_ROW) {
                    this.smoothScrollBy(height, SCROLL_ITEM_TIME);
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && row == SINGLE_ROW) {
                    this.smoothScrollBy(-height, SCROLL_ITEM_TIME);
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * get GridView's item location
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public int getItemCurrentRow() {
        int row = 0;
        int position = 0;
        position = this.getSelectedItemPosition();
        int temp = (position / this.getNumColumns() + 1) % 2;
        if (temp == 0) {
            row = DOUBLE_ROW;
        } else {
            row = SINGLE_ROW;
        }
        return row;
    }

    public int getScrollRow() {
        return scrollRow;
    }

    public void setScrollRow(int scrollRow) {
        this.scrollRow = scrollRow;
    }
}  