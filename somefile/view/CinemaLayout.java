package com.xgimi.gimicinema.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.xgimi.gimicinema.R;
import com.xgimi.gimicinema.model.Constant;
import com.xgimi.gimicinema.utils.FileReadUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pc on 2014/12/10.
 */
public class CinemaLayout extends LinearLayout
        implements ShadowCallBack, OnClickListener, OnFocusChangeListener {
    private static final int COUNT = 15;
    private static final float ZOOM_SCALE = 1.109f;
    SharedPreferences sharedPreferences;

    private ScaleAnimEffect animEffect;
    private ImageView[] shadowBackgrounds = new ImageView[COUNT];
    private ImageView[] arrayOfImageView;
    private FrameLayout[] container = new FrameLayout[COUNT];
    private TextView[] names = new TextView[COUNT];
    private FrameLayout[] frameLayoutViews = new FrameLayout[COUNT];
    private ImageView[] imageViews = new ImageView[COUNT];
    private Context context;

    private String[] keys;
    private String[] defaultValue;

    public CinemaLayout(Context context) {
        super(context);
        sharedPreferences = context.getSharedPreferences(Constant.XML_NAME, Context.MODE_PRIVATE);
        keys = context.getResources().getStringArray(R.array.class_name_setting_show_key);
        defaultValue = context.getResources().getStringArray(R.array.class_name_setting_show_default);

        animEffect = new ScaleAnimEffect();
        this.context = context;
        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(LinearLayout.VERTICAL);
        addView(LayoutInflater.from(context).inflate(R.layout.a_xgimi_main, null));
        initView();
        //first request focus
        imageViews[0].requestFocus();
    }

    public void updateName(String s) {
        if (new File(s + "fl/name1.txt").exists()) {
//            File[] files = new File(s + "fl/").listFiles();
            ArrayList<String> strings = FileReadUtils.readFileByLines(s + "fl/name1.txt");
            HashMap<String, String> name = new HashMap<String, String>(14);
            for (String string : strings) {
                String[] split = string.split("#");
                name.put(split[0], split[1]);
            }
            for (int i = 0 ; i < names.length ; i++) {
                String s1 = name.get(i + "");
                if (!TextUtils.isEmpty(s1)) {
                    names[i].setText(s1);
                }
            }

        }
    }

    public void updateImages(String s, ImageLoader imageLoader) {
        if (new File(s + "fl/name.txt").exists()) {
            File[] files = new File(s + "fl/").listFiles();
            ArrayList<String> strings = FileReadUtils.readFileByLines(s + "fl/name.txt");
            HashMap<String, String> name = new HashMap<String, String>(14);
            for (String string : strings) {
                String[] split = string.split("#");
                name.put(split[0], split[1]);
                Log.d("lovely", "╔════════════════════════════════════════════");
                Log.d("lovely", "╟  " + string);
                Log.d("lovely", "╚════════════════════════════════════════════");
            }

            if (files != null) {
                for (File file : files) {
                    String name1 = getName(file.getAbsolutePath());
                    String s1 = name.get(name1);

                    if (!TextUtils.isEmpty(s1)) {
                        Bitmap bm = ImageReflect.lessenUriImage(file.getAbsolutePath());
                        int i = Integer.parseInt(name1);
                        imageViews[i].setImageBitmap(bm);
                        names[i].setText(s1);
//                        names[i].setShadowLayer(2, 2, 1, Color.BLACK);
                        int cur = -1;
                        switch (i) {
                            case 5:
                                cur = 0;
                                break;
                            case 1:
                                cur = 1;
                                break;
                            case 6:
                                cur = 2;
                                break;
                            case 7:
                                cur = 3;
                                break;
                            case 11:
                                cur = 4;
                                break;
                            case 13:
                                cur = 5;
                                break;
                            case 14:
                                cur = 6;
                                break;
                            case 10:
                                cur = 7;
                                break;
                            default:
                                cur = -1;
                                break;
                        }
                        if (cur != -1) {
                            try {
                                arrayOfImageView[cur].setImageBitmap(ImageReflect.createCutReflectedImage(ImageReflect.convertViewToBitmap(container[i]), 0));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
//            Bitmap bm = ImageReflect.lessenUriImage(s + "fl/14.jpg");
//            imageViews[14].setImageBitmap(bm);
//            arrayOfImageView[6].setImageBitmap(ImageReflect.createCutReflectedImage(ImageReflect.convertViewToBitmap(container[14]), 0));
            }
            //        updateReflectedImage();
//        if (new File(s + "fl").exists()) {
//            if (new File(s + "fl/14.jpg").exists()) {
////                imageLoader.displayImage(s + "fl/14.jpg", imageViews[14]);
//                imageViews[14].setBackground(R.drawable.ic_launcher);
//            }
//        }
        }
    }

    private String getName(String s) {
        int start = s.lastIndexOf("/") + 1;
        int end = s.lastIndexOf(".");
        return s.substring(start, end);
    }

    /**
     * 失去焦点的的动画动作
     *
     * @param paramInt 失去焦点的item
     */
    private void showLoseFocusAnimation(int paramInt) {
        animEffect.setAttributs(ZOOM_SCALE, 1.0F, ZOOM_SCALE, 1.0F, 100L);
        shadowBackgrounds[paramInt].setVisibility(View.GONE);
        boolean b = needScaleAll(paramInt);
        if (!b) {
            container[paramInt].startAnimation(animEffect.createAnimation());
        } else {
            imageViews[paramInt].startAnimation(animEffect.createAnimation());
        }
    }

    private boolean needScaleAll(int paramInt) {
        boolean b = true;
        switch (paramInt) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 6:
            case 7:
            case 10:
            case 9:
            case 11:
            case 12:
            case 13:
            case 14:
                b = false;
                break;
            default:
                b = true;
                break;
        }
        return b;
    }

    /**
     * 获得焦点的item的动画动作
     *
     * @param paramInt 获得焦点的item
     */
    private void showOnFocusAnimation(final int paramInt) {
        this.frameLayoutViews[paramInt].bringToFront();
        this.animEffect.setAttributs(1.0F, ZOOM_SCALE, 1.0F, ZOOM_SCALE, 100L);
        Animation localAnimation = this.animEffect.createAnimation();
        localAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                shadowBackgrounds[paramInt].startAnimation(animEffect.alphaAnimation(0.0F, 1.0F, 150L, 0L));
                shadowBackgrounds[paramInt].setVisibility(View.VISIBLE);
                //实现倒影放大功能，效果不理想，暂不采纳
//                if (paramInt==1) {
//                    arrayOfImageView[1].setImageBitmap(ImageReflect.createCutReflectedImage(ImageReflect.convertViewToBitmap(frameLayoutViews[1]), 0));
//                }else{
//                    arrayOfImageView[1].setImageBitmap(ImageReflect.createCutReflectedImage(ImageReflect.convertViewToBitmap(frameLayoutViews[1]), 0));
//                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });
        boolean b = needScaleAll(paramInt);
        if (!b) {
            container[paramInt].startAnimation(localAnimation);
        } else {
            imageViews[paramInt].startAnimation(localAnimation);
        }
    }

    private OnClickListener onClickListener;

    public void destroy() {
    }

    /**
     * 注册OnClickListener监听
     */
    public void initListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void initListener() {
    }

    public void setOpenSettingListener(OnClickListener onClickListener) {
        findViewById(R.id.openSetting).setOnClickListener(onClickListener);
    }

    /**
     * 初始化视图
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void initView() {
        this.frameLayoutViews[0] = ((FrameLayout) findViewById(R.id.layout_app));
        this.frameLayoutViews[4] = ((FrameLayout) findViewById(R.id.layout_app1));
        this.frameLayoutViews[5] = ((FrameLayout) findViewById(R.id.layout_app2));

        container[1] = ((FrameLayout) findViewById(R.id.frame1));
        container[2] = ((FrameLayout) findViewById(R.id.frame2));
        container[3] = ((FrameLayout) findViewById(R.id.frame3));
        container[4] = ((FrameLayout) findViewById(R.id.frame4));

        container[6] = ((FrameLayout) findViewById(R.id.frame6));
        container[7] = ((FrameLayout) findViewById(R.id.frame7));
        container[10] = ((FrameLayout) findViewById(R.id.frame10));
        container[9] = ((FrameLayout) findViewById(R.id.frame9));

        container[11] = ((FrameLayout) findViewById(R.id.frame11));
        container[12] = ((FrameLayout) findViewById(R.id.frame12));
        container[13] = ((FrameLayout) findViewById(R.id.frame13));
        container[14] = ((FrameLayout) findViewById(R.id.frame14));

        names[1] = ((TextView) findViewById(R.id.nameTv1));
        names[2] = ((TextView) findViewById(R.id.nameTv2));
        names[3] = ((TextView) findViewById(R.id.nameTv3));
        names[4] = ((TextView) findViewById(R.id.nameTv4));

        names[6] = ((TextView) findViewById(R.id.nameTv6));
        names[7] = ((TextView) findViewById(R.id.nameTv7));
        names[10] = ((TextView) findViewById(R.id.nameTv10));
        names[9] = ((TextView) findViewById(R.id.nameTv9));

        names[11] = ((TextView) findViewById(R.id.nameTv11));
        names[12] = ((TextView) findViewById(R.id.nameTv12));
        names[13] = ((TextView) findViewById(R.id.nameTv13));
        names[14] = ((TextView) findViewById(R.id.nameTv14));

        for (int i = 0 ; i < names.length ; i++) {
            if (i == 0 || i == 5 || i == 8) {
                continue;
            }
            names[i].setBackgroundColor(Color.parseColor("#21000000"));
            if (i < 5) {
                names[i].setText(sharedPreferences.getString(keys[i - 1], defaultValue[i - 1]));
                continue;
            }
            if (i < 8) {
                names[i].setText(sharedPreferences.getString(keys[i - 2], defaultValue[i - 2]));
                continue;
            }
            names[i].setText(sharedPreferences.getString(keys[i - 3], defaultValue[i - 3]));
        }

//        names[14].setText(sharedPreferences.getString("tongzhi", "同志"));


        this.frameLayoutViews[1] = ((FrameLayout) findViewById(R.id.layout_game));

        this.frameLayoutViews[11] = ((FrameLayout) findViewById(R.id.layout_game_add));

        this.frameLayoutViews[2] = ((FrameLayout) findViewById(R.id.layout_setting));
        this.frameLayoutViews[6] = ((FrameLayout) findViewById(R.id.layout_setting1));
        this.frameLayoutViews[7] = ((FrameLayout) findViewById(R.id.layout_setting2));

        this.frameLayoutViews[12] = ((FrameLayout) findViewById(R.id.layout_setting_add));
        this.frameLayoutViews[13] = ((FrameLayout) findViewById(R.id.layout_setting1_add));
        this.frameLayoutViews[14] = ((FrameLayout) findViewById(R.id.layout_setting2_add));


        this.frameLayoutViews[3] = ((FrameLayout) findViewById(R.id.layout_code));
        this.frameLayoutViews[8] = ((FrameLayout) findViewById(R.id.layout_code1));

        this.frameLayoutViews[9] = ((FrameLayout) findViewById(R.id.layout_fifth));
        this.frameLayoutViews[10] = ((FrameLayout) findViewById(R.id.layout_fifth1));

        this.shadowBackgrounds[0] = ((ImageView) findViewById(R.id.app_shadow));
        this.shadowBackgrounds[4] = ((ImageView) findViewById(R.id.app_shadow1));
        this.shadowBackgrounds[5] = ((ImageView) findViewById(R.id.app_shadow2));

        this.shadowBackgrounds[1] = ((ImageView) findViewById(R.id.game_shadow));
        this.shadowBackgrounds[11] = ((ImageView) findViewById(R.id.game_shadow_add));

        this.shadowBackgrounds[2] = ((ImageView) findViewById(R.id.setting_shadow));
        this.shadowBackgrounds[6] = ((ImageView) findViewById(R.id.setting_shadow1));
        this.shadowBackgrounds[7] = ((ImageView) findViewById(R.id.setting_shadow2));

        this.shadowBackgrounds[12] = ((ImageView) findViewById(R.id.setting_shadow_add));
        this.shadowBackgrounds[13] = ((ImageView) findViewById(R.id.setting_shadow1_add));
        this.shadowBackgrounds[14] = ((ImageView) findViewById(R.id.setting_shadow2_add));

        this.shadowBackgrounds[3] = ((ImageView) findViewById(R.id.code_shadow));
        this.shadowBackgrounds[8] = ((ImageView) findViewById(R.id.code_shadow1));

        this.shadowBackgrounds[9] = ((ImageView) findViewById(R.id.fifth_shadow));
        this.shadowBackgrounds[10] = ((ImageView) findViewById(R.id.fifth_shadow1));

        this.imageViews[0] = ((ImageView) findViewById(R.id.app));
        this.imageViews[4] = ((ImageView) findViewById(R.id.app1));
        this.imageViews[5] = ((ImageView) findViewById(R.id.app2));

        this.imageViews[1] = ((ImageView) findViewById(R.id.game));
        this.imageViews[11] = ((ImageView) findViewById(R.id.game_add));

        this.imageViews[2] = ((ImageView) findViewById(R.id.setting));
        this.imageViews[6] = ((ImageView) findViewById(R.id.setting1));
        this.imageViews[7] = ((ImageView) findViewById(R.id.setting2));

        this.imageViews[12] = ((ImageView) findViewById(R.id.setting_add));
        this.imageViews[13] = ((ImageView) findViewById(R.id.setting1_add));
        this.imageViews[14] = ((ImageView) findViewById(R.id.setting2_add));

        this.imageViews[3] = ((ImageView) findViewById(R.id.code));
        this.imageViews[8] = ((ImageView) findViewById(R.id.code1));

        this.imageViews[9] = ((ImageView) findViewById(R.id.fifth_img));
        this.imageViews[10] = ((ImageView) findViewById(R.id.fifth_img1));

        arrayOfImageView = new ImageView[9];// 倒影图
        arrayOfImageView[0] = ((ImageView) findViewById(R.id.set_refimg_1));
        arrayOfImageView[1] = ((ImageView) findViewById(R.id.set_refimg_2));
        arrayOfImageView[2] = ((ImageView) findViewById(R.id.set_refimg_3));
        arrayOfImageView[3] = ((ImageView) findViewById(R.id.set_refimg_4));
        arrayOfImageView[4] = ((ImageView) findViewById(R.id.set_refimg_5));
        arrayOfImageView[5] = ((ImageView) findViewById(R.id.set_refimg_6));
        arrayOfImageView[6] = ((ImageView) findViewById(R.id.set_refimg_7));
        arrayOfImageView[7] = ((ImageView) findViewById(R.id.set_refimg_8));
        arrayOfImageView[8] = ((ImageView) findViewById(R.id.set_refimg_9));

        for (int j = 0 ; j < COUNT ; j++) {
            final int finalJ = j;
            imageViews[j].setOnHoverListener(new OnHoverListener() {
                @Override
                public boolean onHover(View v, MotionEvent event) {
                    imageViews[finalJ].requestFocus();
                    return false;
                }
            });
        }
        updateReflectedImage();
    }

    private void updateReflectedImage() {
        for (int j = 0 ; j < COUNT ; j++) {
            this.shadowBackgrounds[j].setVisibility(View.GONE);

            imageViews[j].setOnFocusChangeListener(this);
            imageViews[j].setOnClickListener(this);
            int cur = -1;

            Log.d("lovely", "╔════════════════════════════════════════════");
            Log.d("lovely", "j = " + j);
            Log.d("lovely", "╚════════════════════════════════════════════");

            switch (j) {
                case 5:
                    cur = 0;
                    break;
                case 1:
                    cur = 1;
                    break;
                case 6:
                    cur = 2;
                    break;
                case 7:
                    cur = 3;
                    break;
                case 11:
                    cur = 4;
                    break;
                case 13:
                    cur = 5;
                    break;
                case 14:
                    cur = 6;
                    break;
                case 10:
                    cur = 7;
                    break;/*
                case 14:
                    cur = 8;
                    break;*/
            }
            if (cur != -1) {
                try {
                    Log.d("lovely", "╔════════════════════════════════════════════");
                    Log.d("lovely", "cur = " + cur);
                    Log.d("lovely", "╚════════════════════════════════════════════");
                    arrayOfImageView[cur].setImageBitmap(ImageReflect.createCutReflectedImage(ImageReflect.convertViewToBitmap(imageViews[j]), 0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onClick(View paramView) {
        if (onClickListener != null) {
            onClickListener.onClick(paramView);
        }
    }

    private OnFocusChangeListener onFocusChangeListener;

    /**
     * 注册焦点监听的动作
     */
    public void initListener(OnFocusChangeListener onFocusChangeListener) {
        this.onFocusChangeListener = onFocusChangeListener;
    }

    public void onFocusChange(View paramView, boolean paramBoolean) {
        if (onFocusChangeListener != null) {
            onFocusChangeListener.onFocusChange(paramView, paramBoolean);
        }
        int i = -1;
        switch (paramView.getId()) {
            case R.id.app:
                i = 0;
                break;
            case R.id.app1:
                i = 4;
                break;
            case R.id.app2:
                i = 5;
                break;
            case R.id.game:
                i = 1;
                break;
            case R.id.game_add:
                i = 11;
                break;
            case R.id.setting:
                i = 2;
                break;
            case R.id.setting1:
                i = 6;
                break;
            case R.id.setting2:
                i = 7;
                break;
            case R.id.setting_add:
                i = 12;
                break;
            case R.id.setting1_add:
                i = 13;
                break;
            case R.id.setting2_add:
                i = 14;
                break;
            case R.id.code:
                i = 3;
                break;
            case R.id.code1:
                i = 8;
                break;
            case R.id.fifth_img:
                i = 9;
                break;
            case R.id.fifth_img1:
                i = 10;
                break;
        }
        if (paramBoolean) {
            showOnFocusAnimation(i);
        } else {
            showLoseFocusAnimation(i);
        }
    }

    public void updateData() {
    }
}