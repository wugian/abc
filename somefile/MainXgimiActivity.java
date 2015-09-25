package com.xgimi.gimicinema.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;
import com.xgimi.gimicinema.R;
import com.xgimi.gimicinema.adapter.ClassificationAdapter;
import com.xgimi.gimicinema.db.NewDBManager;
import com.xgimi.gimicinema.model.*;
import com.xgimi.gimicinema.samba.SambaUtil;
import com.xgimi.gimicinema.service.AskService;
import com.xgimi.gimicinema.utils.*;
import com.xgimi.gimicinema.view.MetroGridView;
import com.xgimi.gimicinema.view.MetroLinearLayout;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 李攀 on 2014/12/9.
 */
public class MainXgimiActivity extends BaseActivity implements Preference.OnPreferenceChangeListener {


    private static final int REQUEST_SET = 0x12;
    private static final int SAMBA_MOUNT_FINISH = 0x13;
    private static final int FILE_NOT_EXIST = 0x14;

    private LinearLayout main;
    private MetroLinearLayout recommend;
    private MetroGridView classification;
    private Button transBtn;
    private Button mainSearch;

    private void assignViews() {
        main = (LinearLayout) findViewById(R.id.main);
        recommend = (MetroLinearLayout) findViewById(R.id.recommend);
        classification = (MetroGridView) findViewById(R.id.classification);
        transBtn = (Button) findViewById(R.id.trans_btn);
        mainSearch = (Button) findViewById(R.id.mainSearch);
    }

    private ImageDownload imageDownload;
    //    private CApplication cApplication;
    Context context;
    ScanUtils scanUtils;
    String curPath = "";
    String curFolderName = "";
    String rootPath;

    SambaUtil sambaUtil;
    SharedPreferences sharedPreferences;
    SambaMsg sambaMsg;
    private final static String PASSWORD_STRING = String.valueOf(KeyEvent.KEYCODE_DPAD_UP) +
            String.valueOf(KeyEvent.KEYCODE_DPAD_UP) +
            String.valueOf(KeyEvent.KEYCODE_DPAD_DOWN) +
            String.valueOf(KeyEvent.KEYCODE_DPAD_DOWN);
    private ArrayList<Integer> keyQueue;
    private SystemUtils systemUtils;


    private Handler handler;
    private NewDBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main_contain1);
        MobclickAgent.setCatchUncaughtExceptions(true);
        MobclickAgent.setDebugMode(true);
        imageDownload = new ImageDownload();
        handler = new MainHandler(this);
        dbManager = new NewDBManager(this);
        // get the message from server and charge the led on or off use handler 3min 10min
        sharedPreferences = getSharedPreferences(Constant.XML_NAME, Context.MODE_PRIVATE);
        context = this;
        scanUtils = new ScanUtils(context);
        systemUtils = new SystemUtils();
        // read the path from xml file if null show toast to ask administrator to set it
        initDataFromXml();
        rootPath = sambaMsg.getRootPath();
        sambaUtil = new SambaUtil();
        if (TextUtils.isEmpty(sambaMsg.getIp()) /*|| TextUtils.isEmpty(sambaMsg.getFolder())*/) {
            T.show(context, "samba设置出错，请联系管理员重新设置");
            Intent mIntent = new Intent(this, SettingActivity.class);
            startActivityForResult(mIntent, REQUEST_SET);
        } else {
            mountSamba();
        }
        assignViews();
        initClassification();
        initRecommend();
        bind();
        checkForUpdate();
        View.OnFocusChangeListener l = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lastFocusView = v;
                    if (lastKeyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        recommend.requestFocus();
                    } else {
                        classification.requestFocus();
                    }
                }
            }
        };

        View.OnFocusChangeListener l1 = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lastFocusView = v;
                }
            }
        };
        transBtn.setOnFocusChangeListener(l);
        mainSearch.setOnFocusChangeListener(l1);
    }

    private void initRecommend() {
        Log.d("lovely", System.currentTimeMillis() + "");
        List<LocalMovieMessage> localMovieMessages = dbManager.queryRecommend();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0 ; i < localMovieMessages.size() ; i++) {
            final LocalMovieMessage localMovieMessage = localMovieMessages.get(i);
            final View view = inflater.inflate(R.layout.i_recommend, null);
            final ImageView poster = (ImageView) view.findViewById(R.id.imageView);
            final TextView name = (TextView) view.findViewById(R.id.name_tv);
            final TextView type = (TextView) view.findViewById(R.id.type_tv);
            final TextView length = (TextView) view.findViewById(R.id.length_tv);
            final ImageView imageView = (ImageView) view.findViewById(R.id.play_iv);
            final LinearLayout msglyt = (LinearLayout) view.findViewById(R.id.msg_lyt);
            view.setTag(i + "");
            //if is one startAnimation
            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        Object tag = v.getTag();
                        int i1 = Integer.parseInt((String) tag);
                        if (Math.abs(i1 - recommend.getFocusPosition()) == 1) {
                            if (lastFocusView.getId() == R.id.mainSearch || lastFocusView.getId() == R.id.trans_btn) {
                                recommend.findViewWithTag(recommend.getFocusPosition() + "").requestFocus();
                            } else {
                                recommend.setFocusPosition(i1);
                                startAnimation(v);
                            }
                        } else if (Math.abs(i1 - recommend.getFocusPosition()) == 0) {
                            recommend.setFocusPosition(i1);
                            if (!isFirstFocus) {
                                msglyt.setBackgroundColor(getResources().getColor(R.color.main_red));
                                type.setVisibility(View.VISIBLE);
                                length.setVisibility(View.VISIBLE);
                                imageView.setBackgroundResource(R.drawable.ic_play_focus);
                            } else {
                                startAnimation(v);
                            }
                            isFirstFocus = false;
                        } else {
                            recommend.findViewWithTag(recommend.getFocusPosition() + "").requestFocus();
                        }
                    } else {
                        msglyt.setBackgroundColor(getResources().getColor(R.color.main_gray));
                        type.setVisibility(View.GONE);
                        length.setVisibility(View.GONE);
                        imageView.setBackgroundResource(R.drawable.ic_play_normal);
                    }
                    lastFocusView = v;
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SB.chargePathExist(localMovieMessage.getPlayPath())) {
                        Intent intent = new Intent(context, MovieDetailActivity1.class);
                        intent.putExtra("movie-item", localMovieMessage);
                        intent.putExtra("movie-douban-id", localMovieMessage.getDoubanId());
                        intent.putExtra("current-folder-path", "/adg");
                        startActivity(intent);
                    } else {
                        T.show(context, "电影被移除");
                        finish();
                    }
                    Toast.makeText(context, v.getTag().toString() + "", Toast.LENGTH_SHORT).show();
                }
            });
            int left = 0, right = 0;
            if (i == 0) {
                left = 26;
                right = 0;
            }
            if (i == localMovieMessages.size() - 1) {
                left = 0;
                right = 26;
            }
//            right = 10;
            view.setPadding(left, 38, right, 38);
            if (SB.chargePathExist(localMovieMessage.getPosterPath())) {
                globalImageLoader.displayImage("file://" + localMovieMessage.getPosterPath(), poster);
            } else {
                globalImageLoader.displayImage("drawable://" + R.drawable.ic_movie_default, poster);
            }
            name.setText(localMovieMessage.getMovieName());

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.leftMargin = 5;
            recommend.addView(view, lp);
        }

        System.out.println(localMovieMessages.toString());
        Log.d("lovely", System.currentTimeMillis() + "");
    }

    private boolean isFirstFocus = true;
    ClassificationAdapter classificationAdapter = null;

    ArrayList<ClassificatioinItem> a = new ArrayList<ClassificatioinItem>();

    private void initClassification() {
        String[] ca = getResources().getStringArray(R.array.classification);
        for (String aCa : ca) {
            ClassificatioinItem classificatioinItem = new ClassificatioinItem();
            classificatioinItem.setName(aCa);
            a.add(classificatioinItem);
        }
        classificationAdapter = new ClassificationAdapter(this, a);
        classification.setAdapter(classificationAdapter);

        classification.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                startAnimation_a(view);
                classification.setCurrentPosition(position);
                classificationAdapter.setNotifyDataChange(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        classification.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    classificationAdapter.setNotifyDataChange(-1);
                } else {
//                    startAnimation_a(classification.getChildAt(0));
//                    classification.setSelection(classification.getSelectedItemPosition());
                    classificationAdapter.setNotifyDataChange(classification.getSelectedItemPosition());
                }
            }
        });
        classification.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 12) {
                    Intent mIntent = new Intent(context, SettingActivity.class);
//                    try {
                    startActivityForResult(mIntent, REQUEST_SET);
                    return;
                }
                FolderItem folderItem = new FolderItem();
                folderItem.setFolderName(a.get(position).getName());
                folderItem.setFolderPath(rootPath);
                Intent intent = new Intent(context, NewMovieActivity1.class);
                intent.putExtra("folder-item", folderItem);
                context.startActivity(intent);
            }
        });
    }

    private AnimationSet manimationSet;

    ScaleAnimation scaleAnimation = new ScaleAnimation(1.2f, 1.0f, 1.2f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.4f);
    ScaleAnimation scaleAnimation1 = new ScaleAnimation(1, 1.2f, 1, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.4f);


    private View lastFocusView = null;

    /**
     * 播放动画
     *
     * @param view
     */
    private void startAnimation(View view) {
        AnimationSet animationSet = new AnimationSet(true);
        if (manimationSet != null && manimationSet != animationSet) {
            scaleAnimation.setDuration(200);
            manimationSet.addAnimation(scaleAnimation);
            manimationSet.setFillAfter(false);
            view.startAnimation(manimationSet);
        }
        scaleAnimation1.setDuration(200);
        animationSet.addAnimation(scaleAnimation1);
        animationSet.setFillAfter(true);
        view.startAnimation(animationSet);
        manimationSet = animationSet;
        TextView type = (TextView) view.findViewById(R.id.type_tv);
        TextView length = (TextView) view.findViewById(R.id.length_tv);
        ImageView imageView = (ImageView) view.findViewById(R.id.play_iv);
        LinearLayout msglyt = (LinearLayout) view.findViewById(R.id.msg_lyt);

        msglyt.setBackgroundColor(getResources().getColor(R.color.main_red));
        type.setVisibility(View.VISIBLE);
        length.setVisibility(View.VISIBLE);
        imageView.setBackgroundResource(R.drawable.ic_play_focus);
    }


    private AnimationSet manimationSet_a;

    ScaleAnimation scaleAnimation_a = new ScaleAnimation(1.2f, 1.0f, 1.2f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    ScaleAnimation scaleAnimation1_a = new ScaleAnimation(1, 1.2f, 1, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

    /**
     * 播放动画
     *
     * @param view
     */
    private void startAnimation_a(View view) {
        AnimationSet animationSet = new AnimationSet(true);
        if (manimationSet_a != null && manimationSet_a != animationSet) {
            scaleAnimation_a.setDuration(200);
            manimationSet_a.addAnimation(scaleAnimation_a);
            manimationSet_a.setFillAfter(false);
            view.startAnimation(manimationSet_a);
        }
        scaleAnimation1_a.setDuration(200);
        animationSet.addAnimation(scaleAnimation1_a);
        animationSet.setFillAfter(true);
        view.startAnimation(animationSet);
        manimationSet_a = animationSet;

    }

    private void mountSamba() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                sambaUtil.mount(sambaMsg.getIp(), sambaMsg.getUsr(), sambaMsg.getPwd(), context);
                handler.sendEmptyMessage(SAMBA_MOUNT_FINISH);
            }
        }.start();
    }

    protected void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示");
        builder.setMessage("该设备没有获取授权，点确定可以退出，如需相应的授权请联系极米相关工作人员(yb.xgimi.com).");
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Constant.gimiAuth) {
                    dialog.dismiss();
                } else {
                    myFinish();
                }
            }
        }).show();
    }

    private void myFinish() {
        finish();
    }

    public boolean chargePathExist() {
        boolean b;
        ExecutorService exec = Executors.newCachedThreadPool();
        b = TaskTimeout.execTask(exec, Constant.SB_TIME_OUT, rootPath);
        exec.shutdown();
        System.out.println("End!");
        return b;
    }


    private void initDataFromXml() {
        //get data from xml and set
        String ip = sharedPreferences.getString("ip", "");
        String usr = sharedPreferences.getString("usr", "");
        String pwd = sharedPreferences.getString("pwd", "");
        String folder = sharedPreferences.getString("folder", "");
        String path = sharedPreferences.getString("path", "");

        sambaMsg = new SambaMsg(ip, usr, pwd, folder, path);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - lastDownTime < 200) {
                lastDownTime = System.currentTimeMillis();
                return true;
            }
            lastDownTime = System.currentTimeMillis();
            lastKeyCode = event.getKeyCode();
        }
        return super.dispatchKeyEvent(event);
    }

    private long lastDownTime = 0;
    private int lastKeyCode = -1;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        lastKeyCode = keyCode;
//        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
//            if (keyQueue == null) {
//                keyQueue = new ArrayList<Integer>();
//            }
//            if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
//                keyQueue.add(keyCode);
//            } else {
//                keyQueue.clear();
//            }
//
//            if (keyQueue.size() == 4) {    //add jun.li
//                String keyStr = intArrayListToString(keyQueue);
//                if (keyStr.equals(PASSWORD_STRING)) {
//                    keyQueue.clear();
//                    Intent mIntent = new Intent(this, SettingActivity.class);
//                    try {
//                        startActivityForResult(mIntent, REQUEST_SET);
//                        return true;
//                    } catch (ActivityNotFoundException anf) {
//                        anf.printStackTrace();
//                    }
//                }
//            }
//        }
        //拦截BACK
        if (keyCode == KeyEvent.KEYCODE_BACK) return true;
        else return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SET:
                initDataFromXml();
                rootPath = sambaMsg.getLocalPath();
                break;
        }
    }

    private String intArrayListToString(ArrayList<Integer> al) {
        StringBuilder str = new StringBuilder();
        for (Integer integer : al) {
            str.append(integer);
        }
        return str.toString();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }


    private void bind() {
        //检查服务是否启动，没有启动则重新启动
        if (!systemUtils.isServiceRunning(context, "com.xgimi.gimicinema.service.AskService:remote")) {
            System.out.println("com.xgimi.gimicinema.service.AskService is not running then will start again");
            Intent intent = new Intent(this, AskService.class);
            startService(intent);
        } else {
            System.out.println("com.xgimi.gimicinema.service.AskService is running");
        }
    }


    //for update
    private static final int UPDATE = 0x01;
    private static final int NOT_UPDATE = 0x02;
    private static final int INIT_UPDATE = 0x03;
    private static final int INIT_NEWEST = 0x04;
    private static final int DOWNLOAD_POSTER = 0x05;
    private static final String UPDATE_APK_URL = "http://update.xgimi.com/gimicinema/gimicinema-update.html";
    private static final String UPDATE_POSTER_URL = "http://update.xgimi.com/gimicinema/gimicinema-update-poster.html";

    private ApkMessage apkMessage;
    private PosterMessage posterMessage;
    private boolean isInit = true;


    public void checkForUpdate() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                getServerVerCode();
                updatePoster();
            }
        }.start();
    }

    private boolean getServerVerCode() {
        String verJson = null;
        try {
            verJson = NetworkTool.getContent(UPDATE_APK_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(verJson)) {
            return false;
        }
        try {
            Gson gson = new Gson();
            apkMessage = gson.fromJson(verJson, new TypeToken<ApkMessage>() {
            }.getType());
            if (apkMessage != null) {
                if (apkMessage.getVerCode() > Config.getVerCode(context)) {
                    if (!isInit) {
                        handler.sendEmptyMessage(UPDATE);
                    } else {
                        handler.sendEmptyMessage(INIT_UPDATE);
                        isInit = false;
                    }
                } else {
                    if (!isInit) {
                        handler.sendEmptyMessage(NOT_UPDATE);
                    } else {
                        handler.sendEmptyMessage(INIT_NEWEST);
                        isInit = false;
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void notNewVersionShow() {
        String verName = Config.getVerName(this);
        StringBuilder sb = new StringBuilder();
        sb.append("当前版本");
        sb.append(verName);
        sb.append("已是最新版本");
        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle("软件更新")
                .setMessage(sb.toString())// 设置内容
                .setPositiveButton("确定",// 设置确定按钮
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        }).create();// 创建
        // 显示对话框
        dialog.show();
    }

    private void doNewVersionUpdate() {
        String verName = Config.getVerName(context);
        StringBuffer message = new StringBuffer();
        message.append("当前版本");
        message.append(verName);
        message.append(", 发现新版本");
        message.append(apkMessage.getVerName());
        message.append(", 是否更新?");
        message.append("\n");
        String updateMsg = apkMessage.getUpdateMsg();
        if (!TextUtils.isEmpty(updateMsg)) {
            if (updateMsg.contains("#")) {
                String[] split = updateMsg.split("#");
                for (String s : split) {
                    message.append("\n").append(s);
                }
            } else {
                message.append("\n").append(updateMsg);
            }
/*            message.append("\n").append(apkMessage.getUpdateMsg().replaceAll("\\s", "\n"));*/
        }
        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle("软件更新")
                .setMessage(message.toString())
                .setCancelable(false)
                        // 设置内容
                .setPositiveButton("更新",// 设置确定按钮
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (!TextUtils.isEmpty(apkMessage.getUpdateUrl())) {
                                    UpdateManager updateManager = new UpdateManager(context, apkMessage);
                                    updateManager.showDownloadDialog();
                                } else {
                                    dialog.dismiss();
                                }
                            }

                        }).create();// 创建
        // 显示对话框
        dialog.show();
    }

    private void updatePoster() {
        String verJson = null;
        try {
            verJson = NetworkTool.getContent(UPDATE_POSTER_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(verJson)) {
            return;
        }
        try {
            Gson gson = new Gson();
            posterMessage = gson.fromJson(verJson, new TypeToken<PosterMessage>() {
            }.getType());
            if (posterMessage != null) {
                int time = 0;
                if (posterMessage.getUpdateTime() != time) {
                    handler.sendEmptyMessage(DOWNLOAD_POSTER);
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    private void downloadPoster() {
        File file = new File(String.valueOf(getCacheDir()));
        String[] child = file.list();
        int currentName = 0;
        String oldPoster = "";
        for (String aChild : child) {
            if (aChild.endsWith("jpg") || aChild.endsWith("png")) {
                try {
                    String number = aChild.substring(0, aChild.indexOf("."));
                    currentName = Integer.parseInt(number);
                    oldPoster = getCacheDir() + "/" + aChild;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (currentName != posterMessage.getUpdateTime()) {
            //noinspection ResultOfMethodCallIgnored
            new File(oldPoster).delete();
            downImages(posterMessage.getUpdateUrl(), getCacheDir() + "/" + posterMessage.getUpdateTime() + ".jpg");
        }
    }

    private void downImages(final String urlPath, final String savePath) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                imageDownload.getImage(urlPath, savePath);
            }
        }.start();
    }


    long space = 24 * 60 * 60 * 1000;

    @Override
    protected void onPause() {
        MobclickAgent.onPause(context);
        super.onPause();
    }

    private boolean gimiAuth;

    @Override
    protected void onResume() {
        MobclickAgent.onResume(context);
        super.onResume();
        gimiAuth = Constant.gimiAuth;
        Log.d("lovely", "gimiAuth : " + gimiAuth);
        Log.d("lovely", "Constan.gimiAuth : " + Constant.gimiAuth);
//        if (!gimiAuth) {
//            dialog();
//        }
        long l = System.currentTimeMillis();
        long lastUpdateTime = sharedPreferences.getLong("db-last-update-time-db", 0);
        if (lastUpdateTime == 0) {
            updateThread(l);
        } else if (l - lastUpdateTime > space) {
            updateThread(l);
        }
    }

    private void updateThread(final long l) {
        Log.d("lovely", "do update delete success");
        if (dbHandler == null) {
            dbHandler = new DbHandler(this);
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (NetStatusUtils.isNetworkConnected(context)) {
                    updateDb(l);
                }
            }
        }.start();
    }

    private DbUpdateManger dbUpdateManger;
    private Handler dbHandler;

    private void toast(String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    private void updateDb(long l) {
        if (dbUpdateManger == null) {
            dbUpdateManger = new DbUpdateManger(context, dbHandler);
        }
        if (sambaMsg != null && !TextUtils.isEmpty(sambaMsg.getIp())) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("db-last-update-time-db", l);
            editor.apply();
            dbUpdateManger.insertLocalData();
        }
    }

    public void openSearch(View view) {
        Intent intentSearch = new Intent(this, SearchActivity.class);
        startActivity(intentSearch);
    }

    private static class MainHandler extends Handler {
        private final WeakReference<MainXgimiActivity> activity;

        public MainHandler(MainXgimiActivity activity) {
            this.activity = new WeakReference<MainXgimiActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainXgimiActivity a = activity.get();
            if (a != null) {
                switch (msg.what) {
                    case UPDATE:
                        a.doNewVersionUpdate();
                        break;
                    case NOT_UPDATE:
                        a.notNewVersionShow();
                        break;
                    case INIT_UPDATE:
                        a.doNewVersionUpdate();
                        break;
                    case INIT_NEWEST:
                        //                    checkForUpdate.setText("已是最新版本");
                        break;
                    case 15:
                        Toast.makeText(a.getApplicationContext(), "数据库更新成功", Toast.LENGTH_LONG).show();
                        break;
                    case DOWNLOAD_POSTER:
                        a.downloadPoster();
                        break;
                    case SAMBA_MOUNT_FINISH:
                        if (!SB.chargePathExist(a.sambaMsg.getLocalPath())) {
                            //prompt if need charge network
                            if (NetStatusUtils.isNetworkConnected(a.context)) {
                                T.showLong(a.context, "samba挂载失败，请检测服务器");//网络或者
                            } else {
                                T.showLong(a.context, "网络出问题了");
                            }
                        }
                        break;
                    case FILE_NOT_EXIST:
                        T.showLong(a.context, "路径不存在，请检测网络或者服务器");
                        //remount ?1,charge the ip folder exists or mount
                        //                    if (!SB.chargePathExist("/mnt/samba/" + sambaMsg.getIp())) {
                        if (!SB.chargePathExist(a.sambaMsg.getLocalPath())) {
                            a.mountSamba();
                        } else {
                            T.showLong(a.context, "网络异常");
                        }
                        break;
                }
            }
        }
    }

    private static class DbHandler extends Handler {
        private final WeakReference<MainXgimiActivity> activity;

        public DbHandler(MainXgimiActivity activity) {
            this.activity = new WeakReference<MainXgimiActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainXgimiActivity a = activity.get();
            if (a != null) {
                switch (msg.what) {
                    case Constant.DB_INIT_START:
                        a.toast("开始本地数据初始化");
                        break;
                    case Constant.DB_INIT_SUCCESS:
                        a.toast("本地数据初始化成功" + msg.obj);
                        a.dbUpdateManger.insertDoubanId();
                        break;
                    case Constant.DB_INIT_MSG:
//                        a.initDbBtn.setText(msg.obj + "");
                        break;
                    case Constant.DB_INIT_FAILURE:
                        a.toast("本地数据初始化失败");//应该不可能
                        break;
                    case Constant.DB_INIT_DONE:
                        a.toast("本地数据初始化完成");
//                        SharedPreferences.Editor editor = b.edit();
//                        editor.putLong("db-last-update-time-db", System.currentTimeMillis());
//                        editor.apply();
                        break;
                    case Constant.DB_INIT_ZERO:
                        a.toast("本地数据为空 ");
                        break;
                    case Constant.DB_DOUBANID_INIT_START:
                        a.toast("开始初始化豆瓣ID");
                        break;
                    case Constant.DB_DOUBANID_INIT_SUCCESS:
                        a.toast("初始化豆瓣ID成功 "/* + msg.obj*/);
                        break;
                    case Constant.DB_DOUBANID_INIT_FAILURE:
                        a.toast("初始化豆瓣ID失败");
                        break;
                    case Constant.DB_DOUBANID_INIT_END:
                        a.toast("初始化豆瓣ID完成");
                        a.dbUpdateManger.deleteMovieNotExits();
//                        a.dbUpdateManger.insertType();
                        break;
                    case Constant.DB_TYPE_INIT_START:
                        a.toast("开始初始化类型");
                        break;
                    case Constant.DB_TYPE_INIT_SUCCESS:
                        a.toast("初始化类型成功" + msg.obj);
                        break;
                    case Constant.DB_TYPE_INIT_FAILURE:
                        a.toast("初始化类型失败");
                        break;
                    case Constant.DB_TYPE_INIT_END:
                        a.toast("初始化类型完成");
                        break;
                }
            }
        }
    }
}