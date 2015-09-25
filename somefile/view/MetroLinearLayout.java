package com.xgimi.gimicinema.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by 李攀 on 2015/5/6.
 */
public class MetroLinearLayout extends LinearLayout {
    private int focusPosition = 0;

    public MetroLinearLayout(Context context) {
        super(context);
        setChildrenDrawingOrderEnabled(true);
    }

    public MetroLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setChildrenDrawingOrderEnabled(true);
    }

    public MetroLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setChildrenDrawingOrderEnabled(true);
    }

    public void setFocusPosition(int focusPosition) {
        this.focusPosition = focusPosition;
        invalidate();
    }

    public int getFocusPosition() {
        return focusPosition;
    }

    @Override
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
        super.setChildrenDrawingCacheEnabled(enabled);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (i == childCount - 1) {
            return focusPosition;
        }
        if (i == focusPosition) {
            return childCount - 1;
        }
        return i;
    }
}