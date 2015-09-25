//<MStar Software>
//******************************************************************************
// MStar Software
// Copyright (c) 2010 - 2012 MStar Semiconductor, Inc. All rights reserved.
// All software, firmware and related documentation herein ("MStar Software") are
// intellectual property of MStar Semiconductor, Inc. ("MStar") and protected by
// law, including, but not limited to, copyright law and international treaties.
// Any use, modification, reproduction, retransmission, or republication of all
// or part of MStar Software is expressly prohibited, unless prior written
// permission has been granted by MStar.
//
// By accessing, browsing and/or using MStar Software, you acknowledge that you
// have read, understood, and agree, to be bound by below terms ("Terms") and to
// comply with all applicable laws and regulations:
//
// 1. MStar shall retain any and all ic_right, ownership and interest to MStar
//    Software and any modification/derivatives thereof.
//    No ic_right, ownership, or interest to MStar Software and any
//    modification/derivatives thereof is transferred to you under Terms.
//
// 2. You understand that MStar Software might include, incorporate or be
//    supplied together with third party's software and the use of MStar
//    Software may require additional licenses from third parties.
//    Therefore, you hereby agree it is your sole responsibility to separately
//    obtain any and all third party ic_right and license necessary for your use of
//    such third party's software.
//
// 3. MStar Software and any modification/derivatives thereof shall be deemed as
//    MStar's confidential information and you agree to keep MStar's
//    confidential information in strictest confidence and not disclose to any
//    third party.
//
// 4. MStar Software is provided on an "AS IS" basis without warranties of any
//    kind. Any warranties are hereby expressly disclaimed by MStar, including
//    without limitation, any warranties of merchantability, non-infringement of
//    intellectual property rights, fitness for a particular purpose, error free
//    and in conformity with any international standard.  You agree to waive any
//    claim against MStar for any loss, damage, cost or expense that you may
//    incur related to your use of MStar Software.
//    In no event shall MStar be liable for any direct, indirect, incidental or
//    consequential damages, including without limitation, lost of profit or
//    revenues, lost or damage of data, and unauthorized system use.
//    You agree that this Section 4 shall still apply without being affected
//    even if MStar Software has been modified by MStar in accordance with your
//    request or instruction for your use, except otherwise agreed by both
//    parties in writing.
//
// 5. If requested, MStar may from time to time provide technical supports or
//    services in relation with MStar Software to you for your use of
//    MStar Software in conjunction with your or your customer's product
//    ("Services").
//    You understand and agree that, except otherwise agreed by both parties in
//    writing, Services are provided on an "AS IS" basis and the warranty
//    disclaimer set forth in Section 4 above shall apply.
//
// 6. Nothing contained herein shall be construed as by implication, estoppels
//    or otherwise:
//    (a) conferring any license or ic_right to use MStar name, trademark, service
//        mark, symbol or any other identification;
//    (b) obligating MStar or any of its affiliates to furnish any person,
//        including without limitation, you and your customers, any assistance
//        of any kind whatsoever, or any information; or
//    (c) conferring any license or ic_right under any intellectual property ic_right.
//
// 7. These terms shall be governed by and construed in accordance with the laws
//    of Taiwan, R.O.C., excluding its conflict of law rules.
//    Any and all dispute arising out hereof or related hereto shall be finally
//    settled by arbitration referred to the Chinese Arbitration Association,
//    Taipei in accordance with the ROC Arbitration Law and the Arbitration
//    Rules of the Association by three (3) arbitrators appointed in accordance
//    with the said Rules.
//    The place of arbitration shall be in Taipei, Taiwan and the language shall
//    be English.
//    The arbitration award shall be final and binding to both parties.
//
//******************************************************************************
//<MStar Software>

package com.xgimi.gimicinema.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnTimedTextListener;
import android.media.Metadata;
import android.media.TimedText;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.mstar.android.media.AudioTrackInfo;
import com.mstar.android.media.MMediaPlayer;
import com.mstar.android.media.SubtitleTrackInfo;
import com.mstar.android.media.VideoCodecInfo;
import com.xgimi.gimicinema.R;
import com.xgimi.gimicinema.db.DbManger;
import com.xgimi.gimicinema.interfaces.IPlayerCallback;
import com.xgimi.gimicinema.model.Constants;
import com.xgimi.gimicinema.model.SettingSaveMsg;
import com.xgimi.gimicinema.utils.ISOMountUtils;
import com.xgimi.gimicinema.utils.MD5Utils;
import com.xgimi.gimicinema.utils.SambaFileCharge;
import com.xgimi.gimicinema.utils.Tools;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;


/**
 * VideoPlayView.
 *
 * @author 罗勇 (luoyong@biaoqi.com.cn)
 * @since 1.0
 */

@SuppressLint("NewApi")
public class VideoPlayView extends SurfaceView {

    private static final String TAG = VideoPlayView.class.getSimpleName();

    // settable by the client
    private Uri mUri;
    private int mDuration;
    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int STATE_STREAMLESS_TO_NEXT = 6;
    private static final String MVC = "MVC";

    private static final int KEY_PARAMETER_SET_RESUME_PLAY = 2014;
    private static final int KEY_PARAMETER_SET_DUAL_DECODE_PIP = 2024;
    private static final int KEY_PARAMETER_SET_PQ_LOCAL = 2041;
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private SurfaceHolder mSurfaceHolder = null;

    // private MMediaPlayer mMMMediaPlayer = null;
    // use MMMediaPlayer class for sta
    private MMediaPlayer mMMediaPlayer = null;
    private MMediaPlayer mNextMMediaPlayer = null;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private IPlayerCallback myIPlayerCallback = null;
    private int mSeekWhenPrepared; // recording the seek position while

    private AudioManager mAudioManager = null;

    private boolean isVoiceOpen = true;
    private float currentVoice = 1.0f;
    private int viewId = 0;
    private long startTime;
    private long startSeekTime;
    private long endSeekTime;
    private boolean mbStreamlessOn = true;
    public boolean bResumePlay = false;
    public boolean bSupportDivx = false;
    private Context mContext;
    private Handler mHandler;

    private int subtitleNo = -1;
    private String subtitlePath = null;
    private int subtitleTime = 0;

    public int getSubtitleNo() {
        return subtitleNo;
    }

    public String getSubtitlePath() {
        return subtitlePath;
    }

    public int getSubtitleTime() {
        return subtitleTime;
    }

    public VideoPlayView(Context context) {
        super(context);
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        initVideoView();
    }

    public VideoPlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        initVideoView();
    }

    public VideoPlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        initVideoView();
    }

    public void setStreamlessModeOn(boolean streamlessOn) {
        mbStreamlessOn = streamlessOn;
    }

    public boolean isStreamlessModeOn() {
        int seamstatus = SystemProperties.getInt("mstar.seamlessplay", 0);
        if (seamstatus == 1) {
            mbStreamlessOn = true;
        } else {
            mbStreamlessOn = false;
        }
        return mbStreamlessOn;
    }

    private void copyFile(String srFile, String dtFile) {
        try {
            File f1 = new File(srFile);
            if (!f1.exists()) {
                return;
            }
            File f2 = new File(dtFile);
            if (!f2.exists()) {
                f2.createNewFile();
            }
            FileInputStream in = new FileInputStream(f1);
            FileOutputStream out = new FileOutputStream(f2);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException ex) {
            Log.d("andrew", "the file cannot be found");
        } catch (IOException e) {
            Log.d("andrew", "---IOException---");
        }
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        // getHolder().setFormat(PixelFormat.RGBA_8888);
        getHolder().addCallback(mSHCallback);
        // getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void setVideoPath(String path, int id) {
        this.lookSubtitlePath = null;
        this.videoPath = path;
        if (path == null) {
            return;
        }
        if (path.endsWith("iso")) {
            ISOMountUtils isoMountUtils = new ISOMountUtils(mContext);
            String isoPath = isoMountUtils.getIsoLargeFilePath(path);
            if (isoPath != null) {
                path = isoPath;
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.iso_file_cant_play), Toast.LENGTH_SHORT).show();
//                return;
            }
        }
        path = URLEncoder.encode(path).replace("+", "%20");
        viewId = id;
        if (viewId == 2) {
            String hardwareName = Tools.getHardwareName();
            if (hardwareName.equals("einstein") || hardwareName.equals("napoli") || hardwareName.equals("monaco")) {
                setDualDecodePip(viewId, false);
            }
        }
        mUri = Uri.parse(path);
        mSeekWhenPrepared = 0;
        int md5 = SystemProperties.getInt("mstar.md5", 0);
        if (md5 == 1) {
            int ind = path.lastIndexOf(".");
            if (ind > 0) {
                String sOrgMD5 = path.substring(0, ind) + ".md5";
                int lastInd = path.lastIndexOf("/");
                String sDstMD5 = path.substring(0, lastInd + 1) + "golden.md5";
                copyFile(sOrgMD5, sDstMD5);
            }
        }

        if (isStreamlessModeOn() && (mMMediaPlayer != null) && mMMediaPlayer.isPlaying()) {
            openPlayer2();
        } else {
            openPlayer();
        }
        requestLayout();
        invalidate();
    }

    /**
     * call before play next.
     */
    public void stopPlayback() {
        if (mMMediaPlayer != null && mTargetState != STATE_IDLE) {
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            mMMediaPlayer.stop();
            mMMediaPlayer.reset();
            mMMediaPlayer.release();
            mMMediaPlayer = null;
        }
    }

    /**
     * When abnormal stop play.
     */
    public void stopPlayer() {
        if (mAudioManager != null && viewId == 2) {
            mAudioManager.setParameters("DualAudioOff");
        }
        if (mMMediaPlayer != null && mTargetState != STATE_IDLE) {
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mMMediaPlayer.isPlaying()) {
                        mMMediaPlayer.stop();
                    }
                    mMMediaPlayer.release();
                    mMMediaPlayer = null;
                }
            }).start();
        }
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * When player_a(viewId=1) playes with KEY_PARAMETER_SET_DUAL_DECODE_PIP, set KEY_PARAMETER_SET_DUAL_DECODE_PIP_a true
     * When player_b(viewId=2) playes with KEY_PARAMETER_SET_DUAL_DECODE_PIP, set KEY_PARAMETER_SET_DUAL_DECODE_PIP_b true
     * If KEY_PARAMETER_SET_DUAL_DECODE_PIP_a or KEY_PARAMETER_SET_DUAL_DECODE_PIP_b is true, means KEY_PARAMETER_SET_DUAL_DECODE_PIP
     * already been set for player_a or player_b, so will not set it again.
     */
    public void setDualDecodePip(int viewId, boolean flag) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("VideoPlayView", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (viewId == 2) {
            editor.putBoolean("KEY_PARAMETER_SET_DUAL_DECODE_PIP_b", flag);
            editor.putBoolean("KEY_PARAMETER_SET_DUAL_DECODE_PIP_a", false);
        } else {
            editor.putBoolean("KEY_PARAMETER_SET_DUAL_DECODE_PIP_a", flag);
            editor.putBoolean("KEY_PARAMETER_SET_DUAL_DECODE_PIP_b", false);
        }
        editor.commit();
    }

    public boolean getDualDecodePip() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("VideoPlayView", Context.MODE_PRIVATE);
        boolean flag_a = sharedPreferences.getBoolean("KEY_PARAMETER_SET_DUAL_DECODE_PIP_a", false);
        boolean flag_b = sharedPreferences.getBoolean("KEY_PARAMETER_SET_DUAL_DECODE_PIP_b", false);

        return (flag_a && flag_b);
    }

    /**
     * Start player.
     */
    private void openPlayer() {
        if (mUri == null || mSurfaceHolder == null) {
            return;
        }

        // hideBy3DSetting the built-in music service of android
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        this.getContext().sendBroadcast(i);
        // Close the user's music callback interface
        if (myIPlayerCallback != null)
            myIPlayerCallback.onCloseMusic();

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);
        try {
            mMMediaPlayer = new MMediaPlayer();
            mDuration = -1;
            mMMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMMediaPlayer.setOnErrorListener(mErrorListener);
            mMMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMMediaPlayer.setOnInfoListener(mInfoListener);
            mMMediaPlayer.setOnTimedTextListener(mTimedTextListener);
            mMMediaPlayer.setOnSeekCompleteListener(mMMediaPlayerSeekCompleteListener);
            startTime = System.currentTimeMillis();
            mMMediaPlayer.setDataSource(this.getContext(), mUri);
            mMMediaPlayer.setParameter(KEY_PARAMETER_SET_PQ_LOCAL, 1);
            String hardwareName = Tools.getHardwareName();
            if (viewId == 2) {
                if (hardwareName.equals("einstein") || hardwareName.equals("napoli") || hardwareName.equals("monaco")) {
                    if (!getDualDecodePip()) {
                        Log.i(TAG, "viewId=" + viewId + " setParameter KEY_PARAMETER_SET_DUAL_DECODE_PIP");
                        mMMediaPlayer.setParameter(KEY_PARAMETER_SET_DUAL_DECODE_PIP, 1);
                        setDualDecodePip(viewId, true);
                    }
                }
            }

            if (isStreamlessModeOn()) {
                if (hardwareName.equals("einstein") || hardwareName.equals("napoli") || hardwareName.equals("monaco")) {
                    Log.v(TAG, "einstein/napoli flow set seamless mode E_PLAYER_SEAMLESS_DS");
                    mMMediaPlayer.SetSeamlessMode(MMediaPlayer.EnumPlayerSeamlessMode.E_PLAYER_SEAMLESS_DS);
                }
            }
            if (mSurfaceHolder != null) {
                mMMediaPlayer.setDisplay(mSurfaceHolder);
            }
            if (viewId == 1) {
                mMMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            } else {
                mMMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                if (mAudioManager != null) {
                    mAudioManager.setParameters("DualAudioOn");
                }
            }
            mMMediaPlayer.setScreenOnWhilePlaying(true);
            if (bSupportDivx) {
                if (getResumePlayState()) {
                    bResumePlay = true;
                    mMMediaPlayer.setParameter(KEY_PARAMETER_SET_RESUME_PLAY, 1);
                }
                String fn = getFileName(mUri.getPath());
                SystemProperties.set("mstar.path", fn);
            }

            mMMediaPlayer.prepareAsync();
            // we don't set the target state here either, but preserve the
            // target state that was there before.
            if (isStreamlessModeOn()) {
                if (hardwareName.equals("edison") || hardwareName.equals("kaiser")) {
                    mMMediaPlayer.SetSeamlessMode(MMediaPlayer.EnumPlayerSeamlessMode.E_PLAYER_SEAMLESS_FREEZ);
                    Log.i(TAG, "SetSeamlessMode E_PLAYER_SEAMLESS_FREEZ");
                }
            }

            mCurrentState = STATE_PREPARING;
            mTargetState = STATE_PREPARED;
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            errorCallback(0);
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            errorCallback(0);
        } catch (IllegalStateException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            errorCallback(0);
        } catch (SecurityException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            errorCallback(0);
        }
    }

    /**
     * Start player2.
     */
    private void openPlayer2() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }

        // hideBy3DSetting the built-in music service of android
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        this.getContext().sendBroadcast(i);
        // Close the user's music callback interface
        if (myIPlayerCallback != null)
            myIPlayerCallback.onCloseMusic();

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        // release(false);
        mCurrentState = STATE_STREAMLESS_TO_NEXT;
        try {
            mNextMMediaPlayer = new MMediaPlayer();
            mDuration = -1;
            mNextMMediaPlayer.setOnPreparedListener(mPreparedListener2);
            mNextMMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mNextMMediaPlayer.setOnCompletionListener(mCompletionListener);
            mNextMMediaPlayer.setOnErrorListener(mErrorListener);
            mNextMMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mNextMMediaPlayer.setOnInfoListener(mInfoListener);
            mNextMMediaPlayer.setOnTimedTextListener(mTimedTextListener);
            mNextMMediaPlayer.setOnSeekCompleteListener(mMMediaPlayerSeekCompleteListener);
            startTime = System.currentTimeMillis();
            mNextMMediaPlayer.setDataSource(this.getContext(), mUri);

            if (viewId == 1) {
                mNextMMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            } else {
                mNextMMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                if (mAudioManager != null) {
                    mAudioManager.setParameters("DualAudioOn");
                }
            }
            mNextMMediaPlayer.setScreenOnWhilePlaying(true);

            if (bSupportDivx) {
                if (getResumePlayState()) {
                    bResumePlay = true;
                    mNextMMediaPlayer.setParameter(KEY_PARAMETER_SET_RESUME_PLAY, 1);
                }
                String fn = getFileName(mUri.getPath());
                SystemProperties.set("mstar.path", fn);
            }
            String hardwareName = Tools.getHardwareName();
            if (hardwareName.equals("einstein") || hardwareName.equals("napoli") || hardwareName.equals("monaco")) {

                Log.v(TAG, "einstein/napoli flow set seamless mode E_PLAYER_SEAMLESS_DS");
                mNextMMediaPlayer.SetSeamlessMode(MMediaPlayer.EnumPlayerSeamlessMode.E_PLAYER_SEAMLESS_DS);
            }
            mNextMMediaPlayer.prepareAsync();
            if (hardwareName.equals("edison") || hardwareName.equals("kaiser")) {
                Log.v(TAG, "MediaPlayer2 SetSeamlessMode.....(edison/kaiser flow)");
                mNextMMediaPlayer.SetSeamlessMode(MMediaPlayer.EnumPlayerSeamlessMode.E_PLAYER_SEAMLESS_FREEZ);
            }

            mMMediaPlayer.stop();
            if (hardwareName.equals("nike") || hardwareName.equals("einstein") || hardwareName.equals("napoli") || hardwareName.equals("monaco")) {
                Log.v(TAG, "MediaPlayer1 release.....(nike/einstein/napoli flow)");
                mMMediaPlayer.reset();
                mMMediaPlayer.release();
            } else if (hardwareName.equals("edison") || hardwareName.equals("kaiser")) {
                Log.v(TAG, "MediaPlayer1 setDisplay(null).....(edison/kaiser flow)");
                mMMediaPlayer.setDisplay(null);
            }

            if (mSurfaceHolder != null) {
                Log.v(TAG, "MediaPlayer2 setDisplay.....");
                mNextMMediaPlayer.setDisplay(mSurfaceHolder);
            }

            if (hardwareName.equals("nike")) {
                Log.v(TAG, "MediaPlayer2 SetSeamlessMode.....(nike flow)");
                mNextMMediaPlayer.SetSeamlessMode(MMediaPlayer.EnumPlayerSeamlessMode.E_PLAYER_SEAMLESS_FREEZ);
            }

            mCurrentState = STATE_PREPARING;
            mTargetState = STATE_PREPARED;
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            errorCallback(0);
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            errorCallback(0);
        } catch (IllegalStateException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            errorCallback(0);
        } catch (SecurityException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            errorCallback(0);
        }
    }

    private void errorCallback(int errId) {
        mCurrentState = STATE_ERROR;
        mTargetState = STATE_ERROR;
        if (myIPlayerCallback != null)
            myIPlayerCallback.onError(mMMediaPlayer,
                    MMediaPlayer.MEDIA_ERROR_UNKNOWN, errId, viewId);
    }

    // The following is a series of the player listener in callback
    MMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MMediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            Log.d(TAG, "mVideoWidth: " + mVideoWidth + ", mVideoHeight " + mVideoHeight);
        }
    };

    MMediaPlayer.OnPreparedListener mPreparedListener = new MMediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mCurrentState = STATE_PREPARED;
            if (myIPlayerCallback != null) {
                myIPlayerCallback.onPrepared(mMMediaPlayer, viewId);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            // mSeekWhenPrepared may be changed after seekTo() call
            int seekToPosition = mSeekWhenPrepared;
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            } else {
                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }
            if (Tools.isThumbnailModeOn()) {
                if (mHandler != null) {
                    Message msg = new Message();
                    msg.what = Constants.OpenThumbnailPlayer;
                    //mHandler.sendMessageDelayed(msg, 2000);
                    mHandler.sendMessage(msg);
                }
            }
            loadSubtitle(); // open and set subtitle
        }
    };

    MMediaPlayer.OnPreparedListener mPreparedListener2 = new MMediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mCurrentState = STATE_PREPARING;
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                mNextMMediaPlayer.start();
                String hardwareName = Tools.getHardwareName();
                if (hardwareName.equals("edison") || hardwareName.equals("kaiser")) {
                    mMMediaPlayer.reset();
                    mMMediaPlayer.release();
                }
                mMMediaPlayer = null;
                mMMediaPlayer = mNextMMediaPlayer;
                mNextMMediaPlayer = null;
                mCurrentState = STATE_PLAYING;
            }
            if (myIPlayerCallback != null) {
                myIPlayerCallback.onPrepared(mMMediaPlayer, viewId);
            }
            loadSubtitle(); // open and set subtitle
        }
    };

    private MMediaPlayer.OnCompletionListener mCompletionListener = new MMediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            if (bSupportDivx) {
                setResumePlayState(0);
            }
            if (myIPlayerCallback != null) {
                myIPlayerCallback.onCompletion(mMMediaPlayer, viewId);
            }
        }
    };

    private MMediaPlayer.OnErrorListener mErrorListener = new MMediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.e(TAG, "Error: " + framework_err + "," + impl_err);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;

            /* If an error handler has been supplied, use it and finish. */
            if (myIPlayerCallback != null) {
                if (myIPlayerCallback.onError(mMMediaPlayer, framework_err, impl_err, viewId)) {
                    return true;
                }
            }
            return true;
        }
    };

    private MMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MMediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            if (myIPlayerCallback != null)
                myIPlayerCallback.onBufferingUpdate(mp, percent);
        }
    };

    private MMediaPlayer.OnInfoListener mInfoListener = new MMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (myIPlayerCallback != null) {
                myIPlayerCallback.onInfo(mp, what, extra, viewId);
                return true;
            }
            return false;
        }
    };

    private OnTimedTextListener mTimedTextListener = new OnTimedTextListener() {
        @Override
        public void onTimedText(MediaPlayer arg0, TimedText arg1) {
            if (arg1 != null) {
                if (myIPlayerCallback != null) {
                    myIPlayerCallback.onUpdateSubtitle(arg1.getText());
                }
            } else {
                if (myIPlayerCallback != null) {
                    myIPlayerCallback.onUpdateSubtitle(" ");
                }
            }
        }
    };

    private MMediaPlayer.OnSeekCompleteListener mMMediaPlayerSeekCompleteListener = new MMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            endSeekTime = System.currentTimeMillis();
            Log.i(TAG, ">>>SeekComplete>>>>>>seek time : "
                    + (endSeekTime - startSeekTime) + " ms   viewId:" + viewId);
            setVoice(true);
            if (myIPlayerCallback != null) {
                myIPlayerCallback.onSeekComplete(mp, viewId);
            }
        }
    };

    /**
     * Surface relevant callback interface.
     */
    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                                   int h) {
            mSurfaceHolder = holder;
            /*
             * if (mSurfaceHolder != null && mMMediaPlayer != null ) {
             * mMMediaPlayer.setDisplay(mSurfaceHolder); }
             */
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mMMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            // mSurfaceHolder.setFormat(PixelFormat.RGBA_8888);
            openPlayer();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            if (mAudioManager != null && viewId == 2) {
                mAudioManager.setParameters("DualAudioOff");
            }
            if (viewId != 0)
                release(true);
        }
    };

    /*
     * release the media player in any state.
     */
    private void release(boolean cleartargetstate) {
        if (mTargetState == STATE_IDLE) {
            return;
        }
        mCurrentState = STATE_IDLE;
        if (cleartargetstate) {
            mTargetState = STATE_IDLE;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mMMediaPlayer != null && mMMediaPlayer.isPlaying()) {
                        try {
                            mMMediaPlayer.stop();
                        } catch (IllegalStateException e) {
                            Log.i(TAG, "stop fail! please try again!");
                            try {
                                this.wait(2000);
                                mMMediaPlayer.stop();
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                    if (mMMediaPlayer != null) {
                        mMMediaPlayer.reset();
                        mMMediaPlayer.release();// release will done reset
                    }
                    mMMediaPlayer = null;
                }
            }).start();
        } else {
            if (mMMediaPlayer != null) {
                mMMediaPlayer.reset();
                mMMediaPlayer.release();
            }
            mMMediaPlayer = null;
        }

    }

    public void setPlayingState() {
        mCurrentState = STATE_PREPARED;
        mTargetState = STATE_PLAYING;
    }

    public void start() {
        if (isInPlaybackState()) {
            mMMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    public void pause() {
        if (isInPlaybackState()) {
            if (mMMediaPlayer.isPlaying()) {
                mMMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    /**
     * cache duration as mDuration for faster access.
     *
     * @return mDuration
     */
    public int getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    /**
     * Get the current play time.
     *
     * @return current position
     */
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * Jump to a certain time.
     *
     * @param milliseconds 毫秒
     */
    public void seekTo(int milliseconds) {
        if (isInPlaybackState()) {
            startSeekTime = System.currentTimeMillis();
            mMMediaPlayer.seekTo(milliseconds);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = milliseconds;
        }
    }

    public boolean isPlaying() {
        if (mMMediaPlayer == null) {
            return false;
        }
        try {
            return isInPlaybackState() && mMMediaPlayer.isPlaying();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Determine whether normal play.
     *
     * @return state
     */
    public boolean isInPlaybackState() {
        return (mMMediaPlayer != null && mCurrentState != STATE_ERROR
                && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    public int getMediaParam(int param) {
        Log.i("andrew", "the mMMediaPlayer:" + mMMediaPlayer);
        if (mMMediaPlayer != null) {
            return mMMediaPlayer.getIntParameter(param);
        } else
            return 0;
    }

    private String getFileName(String sPath) {
        String sRes[] = sPath.split("/");
        return sRes[sRes.length - 1];
    }

    public boolean getResumePlayState() {
        if (SystemProperties.getInt("mstar.bootinfo", 1) == 0) {
            if (SystemProperties.getInt("mstar.backstat", 0) == 1) {
                String lPath = SystemProperties.get("mstar.path", "");
                String FN = getFileName(mUri.getPath());
                Log.i("andrew", "the file name is:" + FN);
                if (lPath.equals(FN)) {
                    return true;
                } else {
                    SystemProperties.set("mstar.path", FN);
                    return false;
                }
            } else {
                return false;
            }
        } else {
            SystemProperties.set("mstar.bootinfo", "0");
            return false;
        }
    }

    public void setResumePlayState(int state) {
        String sState = state + "";
        SystemProperties.set("mstar.backstat", sState);
    }

    public void setVideoScale(int leftMargin, int topMargin, int width, int height) {

        LayoutParams lp = getLayoutParams();
        lp.height = height;
        lp.width = width;

        setLayoutParams(lp);
    }

    public void setVideoScaleFrameLayout(int leftMargin, int topMargin, int width, int height) {

        LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            // The following the forced outfit in the decision must be based on
            // the XML type.
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layoutParams;
            params.leftMargin = leftMargin;
            params.rightMargin = leftMargin;
            params.topMargin = topMargin;
            params.bottomMargin = topMargin;
            params.width = width;
            params.height = height;
            setLayoutParams(params);
        }
    }

    public void setVideoScaleLinearLayout(int leftMargin, int topMargin, int width, int height) {

        LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            // The following the forced outfit in the decision must be based on
            // the XML type.
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layoutParams;
            params.leftMargin = leftMargin;
            params.rightMargin = leftMargin;
            params.topMargin = topMargin;
            params.bottomMargin = topMargin;
            params.width = width;
            params.height = height;

            setLayoutParams(params);
        }
    }

    public double calculateZoom(double ScrennWidth, double ScrennHeight) {
        double dRet = 1.0;
        double VideoWidth = (double) mVideoWidth;
        double VideoHeight = (double) mVideoHeight;
        double dw = ScrennWidth / VideoWidth;
        double dh = ScrennHeight / VideoHeight;
        if (dw > dh)
            dRet = dh;
        else
            dRet = dw;

        return dRet;
    }

    public MMediaPlayer getMMediaPlayer() {
        return mMMediaPlayer;
    }

    public void setVoice(boolean isSetOpen) {
        if (isInPlaybackState()) {
            if (isSetOpen) {
                // mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                // currentVoice, AudioManager.FLAG_SHOW_UI);
                mMMediaPlayer.setVolume(currentVoice, currentVoice);
                isVoiceOpen = true;
            } else {
                // currentVoice =
                // mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                // mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                // 0,
                // AudioManager.FLAG_SHOW_UI);
                mMMediaPlayer.setVolume(0, 0);
                isVoiceOpen = false;
            }
        }
    }

    public void setVoice(int voice) {
        if (isInPlaybackState()) {
            if (voice >= 0 && voice <= 10) {
                currentVoice = voice * 0.1f;
            }
            Log.i(TAG, "******currentVoice*******" + currentVoice);
            mMMediaPlayer.setVolume(currentVoice, currentVoice);
        }
    }

    /**
     * Register a callback to be invoked
     *
     * @param l The callback that will be run
     */
    public void setPlayerCallbackListener(IPlayerCallback l) {
        myIPlayerCallback = l;
    }


    /****************************************/
    // mstar Extension APIs start

    /**
     * Set the speed of the video broadcast.
     *
     * @param speed speed
     * @return
     */
    public boolean setPlayMode(int speed) {
        if (speed < -32 || speed > 32)
            return false;

        if (isInPlaybackState()) {
            Log.i(TAG, "****setPlayMode***" + speed);
            return mMMediaPlayer.setPlayMode(speed);
        }
        return false;
    }

    /**
     * For video broadcast speed.
     *
     * @return playMode default 64
     */
    public int getPlayMode() {
        if (isInPlaybackState()) {
            return mMMediaPlayer.getPlayMode();
        }
        return 64;
    }

    /**
     * For track information.
     *
     * @param typeIsAudio
     * @return
     */
    public AudioTrackInfo getAudioTrackInfo(final boolean typeIsAudio) {
        if (isInPlaybackState()) {
            Log.i(TAG, "***getAudioTrackInfo**");
            try {
                return mMMediaPlayer.getAudioTrackInfo(typeIsAudio);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * Settings you want to play the audio track, data from getAudioTrackInfo
     * return values.
     *
     * @param track
     */
    public void setAudioTrack(int track) {
        if (mMMediaPlayer != null) {
            mMMediaPlayer.setAudioTrack(track);
        }
    }


    /**
     * Settings you want to play the subtitles encoding, a video can have
     * multiple subtitles such as English subtitles, Chinese subtitles.
     * 设置内接字幕编号直接调用不要再OPEN OFF
     *
     * @param track trackId
     */
    public void setSubtitleTrack(final int track) {
        if (mMMediaPlayer != null) {
            mMMediaPlayer.setSubtitleDataSource(null);
            mMMediaPlayer.onSubtitleTrack();
            mMMediaPlayer.setSubtitleTrack(track);
            subtitleNo = track;
            subtitlePath = null;
            subtitleTime = 0;
        }
    }

    /**
     * Set up additional caption text.
     * 设置外接字幕路径直接调用不要再OPEN OFF
     *
     * @param uri subtitleUrl
     */
    public void setSubtitleDataSource(final String uri) {
        subtitlePath = uri;
        if (mMMediaPlayer != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (uri != null && (SambaFileCharge.fileExist(uri))) {
                        subtitlePath = uri;
                        mMMediaPlayer.offSubtitleTrack();
                        mMMediaPlayer.setSubtitleDataSource(null);
                        mMMediaPlayer.onSubtitleTrack();
                        mMMediaPlayer.setSubtitleDataSource(subtitlePath);
                        subtitleNo = -1;
                        subtitleTime = 0;
                    }
                }
            }).start();
        }
    }


    private DbManger dbManger;
    private String videoPath;


    //cinema 定制要求加载外接字幕，额外参数寻找字幕路径，允许为空，正常情况下如果曾经设置过则设置了再次字幕，效率影响
    private void loadSubtitle() {
        subtitleNo = -1;
        if (mMMediaPlayer != null) {
            SubtitleTrackInfo subtitleTrackInfo = mMMediaPlayer.getAllSubtitleTrackInfo();
            if (subtitleTrackInfo != null) {
                subtitleNo = subtitleTrackInfo.getAllSubtitleCount();
                if (subtitleNo > 0) {
                    initSubtitleToChinese();
                } else {
                    loadDefaultSubtitle();
                }
            } else {
                loadDefaultSubtitle();
            }
        }
        if (subtitlePath == null) {
            loadDefaultSubtitle();
        }

        File mFile = new File(videoPath);
        long length = mFile.length();
        String md5 = MD5Utils.stringMD5(length + "");

        if (dbManger == null) {
            dbManger = new DbManger(mContext);
        }
        //save subtitle path by md5 逻辑需要修正，如果设置了历史的就不需要再加载默认的了。
        ArrayList<SettingSaveMsg> query = dbManger.query(md5);

        boolean setSubtittle = false;
        for (SettingSaveMsg settingSaveMsg : query) {
            if (settingSaveMsg.getType() == SettingSaveMsg.SETTING_SUBTITLE) {
                try {
                    int i = Integer.parseInt(settingSaveMsg.getValue());
                    setSubtitleTrack(i);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    setSubtitleDataSource(settingSaveMsg.getValue());
                }
                return;
            }
        }
    }

    private String[] innerSubtitle;

    public String[] getInnerSubtitle() {
        return innerSubtitle;
    }

    private void initSubtitleToChinese() {
        SubtitleTrackInfo info = getAllSubtitleTrackInfo();
        int totalInfoNum = info.getAllSubtitleCount();
        innerSubtitle = new String[totalInfoNum];
        info.getSubtitleLanguageType(innerSubtitle, false);
        for (int i = 0 ; i < innerSubtitle.length ; i++) {
            String subtitleLanguageType = innerSubtitle[i];
            if (subtitleLanguageType.equalsIgnoreCase("undefined")) {
                innerSubtitle[i] = mContext.getString(R.string.inner_subtitle_promote) + (i + 1);
            }
        }
        for (int i = 0 ; i < innerSubtitle.length ; i++) {
            String subtitleLanguageType = innerSubtitle[i];
            if (subtitleLanguageType.equalsIgnoreCase("chinese")) {
                setSubtitleTrack(i);
                return;
            }
        }
        setSubtitleTrack(0);
    }

    private String lookSubtitlePath;

    public String getLookSubtitlePath() {
        return lookSubtitlePath;
    }

    public void setLookSubtitlePath(String lookSubtitlePath) {
        this.lookSubtitlePath = lookSubtitlePath;
    }

    public File[] getSubtitleFiles() {
        if (mUri == null) {
            return null;
        }
        String curPath = mUri.getPath();
        if (lookSubtitlePath != null) {
            curPath = lookSubtitlePath;
        }
        File file = new File(curPath);
        File parent = file.getParentFile();
        File[] files =
                parent.listFiles(
                        new FileFilter() {
                            @Override
                            public boolean accept(File pathname) {
                                if (pathname.isDirectory()) {
                                    return false;
                                } else {
                                    String path = pathname.getAbsolutePath().toLowerCase();
                                    if (path.endsWith("aas")) {
                                        return true;
                                    }
                                    if (path.endsWith("srt")) {
                                        return true;
                                    }
                                    if (path.endsWith("idx")) {
                                        return true;
                                    }
                                    if (path.endsWith("ssa")) {
                                        return true;
                                    }
                                    if (path.endsWith("ass")) {
                                        return true;
                                    }
                                    if (path.endsWith("smi")) {
                                        return true;
                                    }
                                }
                                return false;
                            }
                        }

                );
        return files;
    }

    public void loadDefaultSubtitle() {
        if (mMMediaPlayer != null) {
            File[] subtitleFiles = getSubtitleFiles();
            if (subtitleFiles != null && subtitleFiles.length > 0) {
                String path = mUri.getPath();
                if (lookSubtitlePath != null) {
                    path = lookSubtitlePath;
                }
                for (File subtitleFile : subtitleFiles) {
                    if (isSameName(subtitleFile.toString(), path)) {
                        setSubtitleDataSource(subtitleFile.toString());
                    }
                }
            }
        }
    }


    //if subtitle contains chs cht eng 存在不能正确加载的状况暂时没有处理
    private boolean isSameName(String str1, String str2) {
        return (str1).contains(getName(str2));
    }

    private static String getName(String name) {
        int i = name.lastIndexOf("/");
        String name1 = name.substring(i + 1);
        return name1.substring(0, name1.lastIndexOf("."));
    }


    /**
     * Get subtitles data to a string, the string coding unified for utf-8.
     *
     * @return
     */
    public String getSubtitleData() {
        String str = "";
        if (mMMediaPlayer != null) {
            return mMMediaPlayer.getSubtitleData();
        }
        return str;
    }

    /**
     * Set the drawing of subtitles SurfaceHolder.
     *
     * @param sh
     */
    public void setSubtitleDisplay(SurfaceHolder sh) {
        if (mMMediaPlayer != null && sh != null) {
            mMMediaPlayer.setSubtitleDisplay(sh);
        }
    }

    /**
     * get audio codec type.
     *
     * @return
     */
    public String getAudioCodecType() {
        if (isInPlaybackState()) {
            return mMMediaPlayer.getAudioCodecType();
        }
        return null;
    }

    /**
     * get video Info.
     *
     * @return
     */
    public VideoCodecInfo getVideoInfo() {
        if (isInPlaybackState()) {
            return mMMediaPlayer.getVideoInfo();
        }
        return null;
    }

    int syncTime = 0;

    public int getSyncTime() {
        return syncTime;
    }

    /**
     * synchronize subtitle and video.
     *
     * @param time
     * @return
     */
    public int setSubtitleSync(int time) {
        if (isInPlaybackState()) {
            syncTime = time;
            subtitleTime = time;
            return mMMediaPlayer.setSubtitleSync(time);
        }
        return 0;
    }

    /**
     * For a concrete SubtitleTrackInfo object of subtitles.
     *
     * @param subtitlePosition
     * @return
     */
    public SubtitleTrackInfo getSubtitleTrackInfo(int subtitlePosition) {
        if (isInPlaybackState()) {
            return mMMediaPlayer.getSubtitleTrackInfo(subtitlePosition);
        }
        return null;
    }

    /**
     * get subtitle info.
     *
     * @return
     */
    public SubtitleTrackInfo getAllSubtitleTrackInfo() {
        if (isInPlaybackState()) {
            Log.i("****************",
                    "***********mMMediaPlayer.getAllSubtitleTrackInfo()**************");
            return mMMediaPlayer.getAllSubtitleTrackInfo();
        }
        return null;
    }

    public void setSubtitlePath(String subtitlePath) {
        this.subtitlePath = subtitlePath;
    }

    /**
     * check mvc.
     */
    public boolean isMVCSource() {
        if (!isInPlaybackState()) {
            return false;
        } else {
            Metadata data = mMMediaPlayer.getMetadata(true, true);
            if (data == null) {
                return false;
            } else {
                if (data.has(Metadata.VIDEO_CODEC)) {
                    Log.i(TAG, "**VIDEO_CODEC***" + data.getString(Metadata.VIDEO_CODEC));
                }
                if (data.has(Metadata.VIDEO_CODEC)
                        && MVC.equals(data.getString(Metadata.VIDEO_CODEC))) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
    // mstar Extension APIs end
}