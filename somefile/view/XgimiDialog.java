package com.xgimi.gimicinema.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

/**
 * Xgimi统一对话框
 *
 * @author liuyang
 * @tip1 文字行间距是扩大了的，尽量不要超过三行，具体的每个人可以自己改写106行，或者去掉,文字过多也可以调整文字大小
 * @tip2 dialog要先show出来，再设置文字内容，也可以把onCreate里面的方法单独提出来，就可以先设置内容了，如果需要自行修改吧
 * @tiem 2015-04-09
 */
public class XgimiDialog extends Dialog implements OnCancelListener {

    public static final int DIALOG_LEFT = 12345;
    public static final int DIALOG_RIGHT = 54321;
    public static final int DIALOG_CONFIRM = 56789;
    public static final int DIALOG_CANCEL = 98765;

    private final int WIDTH = 440; // 对话框宽度
    private final int HIGHT = 310; // 对画框高度

    private final float RADIUS = 10;

    private final float TEXTSIZE_TITLE = 28;
    private final float TEXTSIZE_TIP = 23; // 可调整提示文字大小
    private final float TEXTSIZE_BUTTON = 23;

    private final int BUTTONLAYOUTPAD = 9;

    private final int color_Normal = 0xff434343;
    private final int color_Focused = 0xff33b5e5;
    private final int color_Pressed = 0xff0099cc;

    private final int TITLETEXTCOLOR = 0xff6b6b6b;
    private final int TIPTEXTCOLOR = 0x8b535353;

    private Handler mHandler = new Handler();

    private FrameLayout root_Layout;
    private LinearLayout tip_Layout, btn_Layout;

    private TextView _title, _tip;

    private Button btn_Left, btn_Right, btn_confirm;

    public XgimiDialog(Context context) {
        super(context);
    }

    public XgimiDialog(Context context, Handler handler) {
        super(context);
        this.mHandler = handler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        root_Layout = new FrameLayout(getContext());
        tip_Layout = new LinearLayout(getContext());
        btn_Layout = new LinearLayout(getContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(root_Layout);
        refreshDialog(WIDTH, HIGHT);
        setOnCancelListener(this);

        _title = new TextView(getContext());
        _title.setTextSize(TEXTSIZE_TITLE);
        _title.setTextColor(TITLETEXTCOLOR);

        LinearLayout.LayoutParams params_title = new LinearLayout.LayoutParams(-2, -1);
        params_title.topMargin = 35;
        tip_Layout.addView(_title, params_title);

        ImageView image_line = new ImageView(getContext());
        image_line.setBackgroundColor(TITLETEXTCOLOR);
        image_line.setAlpha(0.2f);

        LinearLayout.LayoutParams params_line = new LinearLayout.LayoutParams(-1, 1);
        params_line.topMargin = 5;
        tip_Layout.addView(image_line, params_line);

        _tip = new TextView(getContext());
        _tip.setLineSpacing(0, 1.3f); // 增大了文字行间距
        _tip.setTextSize(TEXTSIZE_TIP);
        _tip.setTextColor(TIPTEXTCOLOR);

        LinearLayout.LayoutParams params_tip = new LinearLayout.LayoutParams(-2, -2);
        params_tip.topMargin = 15;
        tip_Layout.addView(_tip, params_tip);

        tip_Layout.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams params_tiplayout = new FrameLayout.LayoutParams(-1, -2);
        params_tiplayout.leftMargin = 40;
        params_tiplayout.rightMargin = 40;
        root_Layout.addView(tip_Layout, params_tiplayout);

        btn_Left = getRadiusButton(new float[]{0, 0, 0, 0, 0, 0, RADIUS, RADIUS});
        LinearLayout.LayoutParams param_left = new LinearLayout.LayoutParams(-1, -1, 1.0f);
        param_left.leftMargin = BUTTONLAYOUTPAD;
        btn_Layout.addView(btn_Left, param_left);
        btn_Left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(DIALOG_LEFT);
                }
            }

        });

        btn_Right = getRadiusButton(new float[]{0, 0, 0, 0, RADIUS, RADIUS, 0, 0});
        LinearLayout.LayoutParams param_right = new LinearLayout.LayoutParams(-1, -1, 1.0f);
        param_right.rightMargin = BUTTONLAYOUTPAD;
        btn_Layout.addView(btn_Right, param_right);
        btn_Right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(DIALOG_RIGHT);
                }
            }

        });

        btn_confirm = getRadiusButton(new float[]{0, 0, 0, 0, RADIUS, RADIUS, RADIUS, RADIUS});
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(DIALOG_CONFIRM);
                }
            }

        });

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(DIALOG_CANCEL);
        }

    }

    /**
     * 设置文字,需要先show出来!
     *
     * @param title 标题
     * @param tip   信息提示
     * @param left  左边按钮的文字
     * @param right 右边按钮的文字
     */
    public void setDialogText(String title, String tip, String left, String right) {
        _title.setText(title);
        _tip.setText(tip);
        btn_Left.setText(left);
        btn_Right.setText(right);

        if (btn_Layout.getParent() == null) {
            FrameLayout.LayoutParams params_btnlayout = new FrameLayout.LayoutParams(-1, getDensityPX(70));
            params_btnlayout.gravity = Gravity.BOTTOM;
            params_btnlayout.bottomMargin = BUTTONLAYOUTPAD;
            root_Layout.addView(btn_Layout, params_btnlayout);
        }
    }

    /**
     * 单选项对话框,需要先show出来!
     *
     * @param tip     提示文字
     * @param confirm 确定按钮文字
     */
    public void setDialogText(String title, String tip, String confirm) {
        _title.setText(title);
        _tip.setText(tip);
        btn_confirm.setText(confirm);

        if (btn_confirm.getParent() == null) {
            FrameLayout.LayoutParams params_btnlayout = new FrameLayout.LayoutParams(-1, getDensityPX(70));
            params_btnlayout.gravity = Gravity.BOTTOM;
            params_btnlayout.bottomMargin = BUTTONLAYOUTPAD;
            params_btnlayout.leftMargin = BUTTONLAYOUTPAD;
            params_btnlayout.rightMargin = BUTTONLAYOUTPAD;
            root_Layout.addView(btn_confirm, params_btnlayout);
        }
    }

    /**
     * 改变title文字与顶部间隔
     *
     * @param topMargin title文字与顶部间隔
     */
    public void changeTitleTopMargin(int topMargin) {
        ((FrameLayout.LayoutParams) _title.getLayoutParams()).topMargin = topMargin;

    }

    /**
     * 对话框显示布局
     *
     * @param w 宽
     * @param h 高
     */
    private void refreshDialog(float w, float h) {
        Window window = getWindow();
        window.setWindowAnimations(android.R.style.Animation);
        window.setDimAmount(0.65f);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = getDensityPX(w);
        wl.height = getDensityPX(h);
        window.setBackgroundDrawable(drawShadow(getRectBitmap(wl.width, wl.height, 0xffe5e4e4, (int) RADIUS), 10));
        onWindowAttributesChanged(wl);
        setCanceledOnTouchOutside(true);
    }

    private Button getRadiusButton(float[] outerR) {
        Button button = new Button(getContext());
        button.setTextColor(Color.WHITE);
        button.setTextSize(TEXTSIZE_BUTTON);
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, getRectShape(outerR, color_Pressed));
        drawable.addState(new int[]{android.R.attr.state_focused}, getRectShape(outerR, color_Focused));
        drawable.addState(new int[]{}, getRectShape(outerR, color_Normal));
        button.setBackground(drawable);
        return button;
    }

    private ShapeDrawable getRectShape(float[] outerR, int color) {
        RoundRectShape roundRectShape = new RoundRectShape(outerR, null, null); // 构造一个圆角矩形,可以使用其他形状，这样ShapeDrawable
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape); // 组合圆角矩形和ShapeDrawable
        shapeDrawable.getPaint().setColor(color); // 设置形状的颜色
        return shapeDrawable;
    }

    @SuppressWarnings("deprecation")
    private Drawable drawShadow(Bitmap bitmap, int radius) {
        Bitmap map = bitmap;
        BlurMaskFilter blurFilter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.OUTER);
        Paint shadowPaint = new Paint();
        shadowPaint.setMaskFilter(blurFilter);
        int[] offsetXY = new int[2];
        Bitmap shadowImage = map.extractAlpha(shadowPaint, offsetXY);
        shadowImage = shadowImage.copy(Config.ARGB_8888, true);
        if (!shadowImage.isPremultiplied()) {
            shadowImage.setPremultiplied(true);
        }
        Canvas c = new Canvas(shadowImage);
        c.drawBitmap(map, -offsetXY[0], -offsetXY[1], null);
        return new BitmapDrawable(shadowImage);
    }

    private Bitmap getRectBitmap(int x, int y, int color, int radius) {
        Bitmap output = Bitmap.createBitmap(x, y, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        RectF outerRect = new RectF(0, 0, x, y);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        canvas.drawRoundRect(outerRect, radius, radius, paint);
        canvas.save();
        canvas.restore();
        return output;
    }


    //获取绝对长度
    private int getDensityPX(float data) {
        return (int) (data * getContext().getResources().getDisplayMetrics().density);
    }

}