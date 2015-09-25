package com.xgimi.gimicinema.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xgimi.gimicinema.R;



public class SwitchView extends LinearLayout implements OnClickListener {

    private static final int FLAG_MOVE_LEFT = 1; // 向左滑动标识
    private static final int FLAG_MOVE_RIGHT = 2; // 向右滑动标识

    private static final int HANDLE_LAYOUT_CURSOR = 100; // 处理调用开关的layout方法

    private Context context; // 上下文对象
    private FrameLayout container; // SwitchView的外层Layout
    private ImageView slideIv; // 开关邮标的ImageView,滑块
    private TextView leftTv; // true的文字信息控件
    private TextView rightTv; // false的文字信息控件

    private boolean isChecked = true; // 是否已开
    private boolean checkedChange = false; // isChecked是否有改变
    private OnCheckedChangeListener onCheckedChangeListener; // 用于监听isChecked是否有改变

    private int margin = 1; // 游标离边缘位置(这个值视图片而定, 主要是为了图片能显示正确)
    private int bg_left; // 背景左
    private int bg_right; // 背景右
    private int cursor_left; // 游标左部
    private int cursor_top; // 游标顶部
    private int cursor_right; // 游标右部
    private int cursor_bottom; // 游标底部

    private Animation animation; // 移动动画
    private int currentFlag = FLAG_MOVE_LEFT; // 当前移动方向flag

    //add
    private int textNormalColor = android.R.color.black;//文字normal颜色
    private int textFocusColor = android.R.color.black;//文字focus颜色
    private int textSize = 18;//文字大小
    private String textLeft = "open";//左边文字
    private String textRight = "close";//右边文字
    private int slideDrawable;//滑块背景
    private int slideBgDrawable;//总体背景

    public SwitchView(Context context) {
        super(context);
        this.context = context;
        initView(null);
    }

    public SwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView(attrs);
    }

    public SwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs);
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public SwitchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        this.context = context;
//        initView(attrs);
//    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 获取所需要的值
        bg_left = container.getLeft();
        bg_right = container.getRight();
        cursor_left = slideIv.getLeft();
        cursor_top = slideIv.getTop();
        cursor_right = slideIv.getRight();
        cursor_bottom = slideIv.getBottom();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_LAYOUT_CURSOR:
                    slideIv.layout(cursor_left, cursor_top, cursor_right, cursor_bottom);
                    break;
            }
        }
    };

    public void onClick(View v) {
        // 控件点击时触发改变checked值
        if (v == this) {
            changeChecked(!isChecked);
        }
    }

    /**
     * 初始化控件
     */
    private void initView(AttributeSet attrs) {

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchView);

            textSize = typedArray.getInt(R.styleable.SwitchView_text_size, 18);
            textNormalColor = typedArray.getColor(R.styleable.SwitchView_normal_text_color, android.R.color.black);
            textFocusColor = typedArray.getColor(R.styleable.SwitchView_focus_text_color, android.R.color.black);
            slideDrawable = (typedArray.getResourceId(R.styleable.SwitchView_slide_drawable, android.R.drawable.dialog_holo_light_frame));
            slideBgDrawable = (typedArray.getResourceId(R.styleable.SwitchView_slide_bg_drawable, android.R.drawable.editbox_dropdown_dark_frame));
            textLeft = getResources().getString(typedArray.getResourceId(R.styleable.SwitchView_text_left, android.R.string.yes));
            textRight = getResources().getString(typedArray.getResourceId(R.styleable.SwitchView_text_right, android.R.string.no));

            typedArray.recycle();
        }


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.switch_view, this);
        view.setOnClickListener(this);
        container = (FrameLayout) view.findViewById(R.id.container_lyt);
        leftTv = (TextView) view.findViewById(R.id.text_left);
        rightTv = (TextView) view.findViewById(R.id.text_right);
        //text color size
        leftTv.setText(textLeft);
        rightTv.setText(textRight);
        leftTv.setTextSize(textSize);
        rightTv.setTextSize(textSize);
        //slide background
        slideIv = (ImageView) view.findViewById(R.id.switch_cursor_iv);
        slideIv.setClickable(false);
        slideIv.setBackgroundResource(slideDrawable);
        container.setBackgroundResource(slideBgDrawable);

        changeTextColor();

        slideIv.setOnTouchListener(new OnTouchListener() {
            int lastX; // 最后的X坐标

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();

                        cursor_left = v.getLeft();
                        cursor_top = v.getTop();
                        cursor_right = v.getRight();
                        cursor_bottom = v.getBottom();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;

                        cursor_left = v.getLeft() + dx;
                        cursor_right = v.getRight() + dx;

                        // 超出边界处理
                        if (cursor_left <= bg_left + margin) {
                            cursor_left = bg_left + margin;
                            cursor_right = cursor_left + v.getWidth();
                        }
                        if (cursor_right >= bg_right - margin) {
                            cursor_right = bg_right - margin;
                            cursor_left = cursor_right - v.getWidth();
                        }
                        v.layout(cursor_left, cursor_top, cursor_right, cursor_bottom);

                        lastX = (int) event.getRawX();
                        break;
                    case MotionEvent.ACTION_UP:
                        calculateIscheck();
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 计算处于true或是false区域, 并做改变处理
     */
    private void calculateIscheck() {
        float center = (float) ((bg_right - bg_left) / 2.0);
        float cursor_center = (float) ((cursor_right - cursor_left) / 2.0);
        if (cursor_left + cursor_center <= center) {
            changeChecked(true);
        } else {
            changeChecked(false);
        }
    }

    /**
     * 改变checked, 根据checked移动游标
     *
     * @param isChecked
     */
    private void changeChecked(boolean isChecked) {

        if (this.isChecked != isChecked) {
            checkedChange = true;
        } else {
            checkedChange = false;
        }
        if (isChecked) {
            currentFlag = FLAG_MOVE_LEFT;
        } else {
            currentFlag = FLAG_MOVE_RIGHT;
        }
        cursorMove();
    }

    /**
     * 游标移动
     */
    private void cursorMove() {
        // 这里说明一点, 动画本可设置animation.setFillAfter(true)
        // 令动画进行完后停在最后位置. 但这里使用这样方式的话.
        // 再次拖动图片会出现异常(具体原因我没找到)
        // 所以最后只能使用onAnimationEnd回调方式再layout游标
        animation = null;
        final int toX;
        if (currentFlag == FLAG_MOVE_LEFT) {
            toX = cursor_left - bg_left - margin;
            animation = new TranslateAnimation(0, -toX, 0, 0);
        } else {
            toX = bg_right - margin - cursor_right;
            animation = new TranslateAnimation(0, toX, 0, 0);
        }
        animation.setDuration(100);
        animation.setInterpolator(new LinearInterpolator());
        animation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationRepeat(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {
                // 计算动画完成后游标应在的位置
                if (currentFlag == FLAG_MOVE_LEFT) {
                    cursor_left -= toX;
                    cursor_right = cursor_left + slideIv.getWidth();
                } else {
                    cursor_right = bg_right - margin;
                    cursor_left = cursor_right - slideIv.getWidth();
                }
                // 这里不能马上layout游标正确位置, 否则会有一点点闪屏
                // 为了美观, 这里迟了一点点调用layout方法, 便不会闪屏
                mHandler.sendEmptyMessageDelayed(HANDLE_LAYOUT_CURSOR, 5);
                // 这里是根据是不是改变了isChecked值进行一些操作
                if (checkedChange) {
                    isChecked = !isChecked;
                    if (onCheckedChangeListener != null) {
                        onCheckedChangeListener.onCheckedChanged(isChecked);
                    }
                    changeTextColor();
                }
            }
        });
        slideIv.startAnimation(animation);
    }

    /**
     * 改变字体显示颜色
     */
    private void changeTextColor() {
        if (isChecked) {
            leftTv.setTextColor(textFocusColor);
            rightTv.setTextColor(textNormalColor);
        } else {
            leftTv.setTextColor(textNormalColor);
            rightTv.setTextColor(textFocusColor);
        }
    }

    /**
     * layout游标
     */
    private void layoutCursor() {
        if (isChecked) {
            cursor_left = bg_left + margin;
            cursor_right = bg_left + margin + slideIv.getWidth();
        } else {
            cursor_left = bg_right - margin - slideIv.getWidth();
            cursor_right = bg_right - margin;
        }
        slideIv.layout(cursor_left, cursor_top, cursor_right, cursor_bottom);
    }

    /**
     * isChecked值改变监听器
     */
    public interface OnCheckedChangeListener {
        void onCheckedChanged(boolean isChecked);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        if (this.isChecked != isChecked) {
            this.isChecked = isChecked;
            changeTextColor();
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChanged(isChecked);
            }
            layoutCursor();
        }
    }

    public void setOnCheckedChangeListener(
            OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

}