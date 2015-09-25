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
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import com.mstar.android.media.MMediaPlayer;
import com.xgimi.gimicinema.model.Constants;
import com.xgimi.gimicinema.utils.Tools;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VideoGLSurfaceView extends GLSurfaceView {

    private static final int PIXEL_FORMAT = GLES20.GL_RGB;
    private static final boolean SET_CHOOSER = PIXEL_FORMAT == GLES20.GL_RGBA ? true : false;
    private static final String TAG = "VideoGLSurfaceView";
    private static final int MEDIA_PLAYER_IDEL = 0;
    private static final int MEDIA_PLAYER_PREPARED = 1;
    private static final int MEDIA_PLAYER_STARTED = 2;
    private static final int MEDIA_PLAYER_PAUSED = 3;
    private static final int MEDIA_PLAYER_STOPPED = 4;
    private static final int MEDIA_PLAYER_PLAYBACK_COMPLETE = 5;
    private static final int MEDIA_PLAYER_ERROR = 6;
    private static final int KEY_PARAMETER_SET_DUAL_DECODE_PIP = 2024;
    private static final int KEY_PARAMETER_SET_MULTI_THUMBS = 2040;

    private Context mContext;
    VideoDumpRenderer mRenderer;
    private Handler mHandler = null;

    private SurfaceTexture mSurfaceTexture = null;
    private MMediaPlayer mThumbnailMMediaPlayer = null;
    private int mCurrentState = MEDIA_PLAYER_IDEL;
    private int mDuration;
    private int mSeekPosition;
    private int mThumbnailInterval;
    private int mThumbnailNumber;
    private Thread mSurfaceTextureThread = null;

    public VideoGLSurfaceView(Context context) {
        super(context);
        Log.i(TAG, "VideoGLSurfaceView 1");
        initVideoView(context);
    }

    public VideoGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "VideoGLSurfaceView 2");
        initVideoView(context);
    }

    private void initVideoView(Context context) {
        mContext = context;
        setEGLContextClientVersion(2);
        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mRenderer = new VideoDumpRenderer(context);
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void init(Handler handler, String videoPath) {
        this.mHandler = handler;
        setRenderMode(RENDERMODE_CONTINUOUSLY);
        requestRender();
        openThumbnailPlayer(videoPath);
    }

    public void doClear() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("VideoGLSurfaceView", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("SeekBarOnHover", false);
        editor.putBoolean("SET_MULTI_THUMBS", true);
        editor.commit();
        Message msg = new Message();
        msg.what = Constants.HideThumbnailBorderView;
        if (mHandler != null) {
            mHandler.sendMessageDelayed(msg, 500);
            // mHandler.sendMessage(msg);
        }
        queueEvent(new Runnable() {
            public void run() {
                mRenderer.doClear();
            }
        });
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause ---------------- begin");
        releaseThumbnailPlayer(true);
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume --------------------- begin");
        super.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!Tools.isThumbnailModeOn()) {
            return true;
        }
        Log.i(TAG, "onTouchEvent ------------------ begin");

        SharedPreferences sharedPreferences = mContext.getSharedPreferences("VideoGLSurfaceView", Context.MODE_PRIVATE);
        int surfaceWidth = sharedPreferences.getInt("SurfaceWidth", 225); // defalut SurfaceWidth is  225
        boolean seekBarOnHover = sharedPreferences.getBoolean("SeekBarOnHover", false);
        boolean thumbnailBorderViewFocusFlag = sharedPreferences.getBoolean("ThumbnailBorderViewFocus", false);
        boolean canSetMultiThumbs = sharedPreferences.getBoolean("SET_MULTI_THUMBS", false);
        if (!canSetMultiThumbs) {
            return true;
        }
        queueEvent(new Runnable() {
            public void run() {
                mRenderer.doClear();
            }
        });

        if (!thumbnailBorderViewFocusFlag) {
            return true;
        }

        int[] imageLeftX = new int[5];
        int[] imageRightX = new int[5];
        for (int i = 0; i < 5; i++) {
            imageLeftX[i] = 15 * i + surfaceWidth * i; // two images's interval is 15
            imageRightX[i] = 15 * i + surfaceWidth * (i + 1);
            Log.i(TAG, "i:" + i + "=(" + imageLeftX[i] + " ," + imageRightX[i] + ")");
        }
        int getX = (int) motionEvent.getX();
        int getY = (int) motionEvent.getY();
        Log.i(TAG, "onTouchEvent -- X:" + getX + " Y:" + getY);

        int index = 0;
        if (getX < imageRightX[0]) {
            index = 0;
        } else if ((getX > imageLeftX[1]) && (getX < imageRightX[1])) {
            index = 1;
        } else if ((getX > imageLeftX[2]) && (getX < imageRightX[2])) {
            index = 2;
        } else if ((getX > imageLeftX[3]) && (getX < imageRightX[3])) {
            index = 3;
        } else if ((getX > imageLeftX[4]) && (getX < imageRightX[4])) {
            index = 4;
        }

        Message msg = new Message();
        msg.what = Constants.SeekWithHideThumbnailBorderView;
        msg.arg1 = sharedPreferences.getInt("TextureTimeStamp" + index, mSeekPosition);
        Log.i(TAG, "Thumbnail index:" + index + " TextureTimeStamp:" + msg.arg1);
        if (mHandler != null) {
            mHandler.sendMessage(msg);
        }

        doClear();
        return true;
    }

    private void openThumbnailPlayer(String videoPath) {
        releaseThumbnailPlayer(false);

        SharedPreferences sharedPreferences = mContext.getSharedPreferences("VideoGLSurfaceView", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("SeekBarOnHover", false);
        editor.putBoolean("ThumbnailOnHover", false);
        editor.putBoolean("SET_MULTI_THUMBS", true);
        editor.commit();

        mCurrentState = MEDIA_PLAYER_IDEL;
        Uri uri = Uri.parse(videoPath);
        try {
            mThumbnailMMediaPlayer = new MMediaPlayer();
            mThumbnailMMediaPlayer.setOnPreparedListener(mThumbnailPreparedListener);
            mThumbnailMMediaPlayer.setOnErrorListener(mErrorListener);
            mThumbnailMMediaPlayer.setDataSource(mContext, uri);
            mThumbnailMMediaPlayer.setParameter(KEY_PARAMETER_SET_DUAL_DECODE_PIP, 1);

            getThumbnailFrame(-1, 5, 2000); // initialization
            createSurfaceTextureThread();
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + uri, ex);
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + uri, ex);
            return;
        } catch (IllegalStateException ex) {
            Log.w(TAG, "Unable to open content: " + uri, ex);
            return;
        } catch (SecurityException ex) {
            Log.w(TAG, "Unable to open content: " + uri, ex);
            return;
        }
    }

    @SuppressLint("NewApi")
    private void createSurfaceTextureThread() {
        mSurfaceTextureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                int[] textureID = new int[1];
                GLES20.glGenTextures(1, textureID, 0);
                checkGlError("glGenTextures");
                mSurfaceTexture = new SurfaceTexture(textureID[0], true);
                mHandler.sendEmptyMessage(Constants.PrepareMediaPlayer);
                Looper.loop();
            }
        });
        mSurfaceTextureThread.start();
    }

    public void prepareMediaPlayer() {
        try {
            Surface surface = new Surface(mSurfaceTexture);
            mThumbnailMMediaPlayer.setSurface(surface);
            surface.release();

            queueEvent(new Runnable() {
                public void run() {
                    mRenderer.setupFramebuffer();
                    mRenderer.setSurfaceTexture(mSurfaceTexture);
                    mRenderer.setHandler(mHandler);
                }
            });
            mThumbnailMMediaPlayer.prepareAsync();
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "IllegalArgumentException", ex);
            return;
        } catch (IllegalStateException ex) {
            Log.w(TAG, "IllegalStateException", ex);
            return;
        } catch (SecurityException ex) {
            Log.w(TAG, "SecurityException", ex);
            return;
        }
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    MMediaPlayer.OnPreparedListener mThumbnailPreparedListener = new MMediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mCurrentState = MEDIA_PLAYER_PREPARED;
            mThumbnailMMediaPlayer.start();
            mCurrentState = MEDIA_PLAYER_STARTED;
        }
    };

    MMediaPlayer.OnErrorListener mErrorListener = new MMediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.e(TAG, "Error: " + framework_err + "," + impl_err);
            mCurrentState = MEDIA_PLAYER_ERROR;
            /* If an error handler has been supplied, use it and finish. */
            releaseThumbnailPlayer(false);
            return true;
        }
    };

    public MMediaPlayer getThumbnailMMediaPlayer() {
        return mThumbnailMMediaPlayer;
    }

    /**
     * Determine whether normal play.
     *
     * @return
     */
    public boolean isInPlaybackState() {
        return ((mThumbnailMMediaPlayer != null) && (mCurrentState != MEDIA_PLAYER_IDEL) && (mCurrentState != MEDIA_PLAYER_ERROR));
    }

    public int getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0) {
                Log.i(TAG, "getDuration mDuration1:" + mDuration);
                return mDuration;
            }
            mDuration = mThumbnailMMediaPlayer.getDuration();
            Log.i(TAG, "getDuration mDuration2:" + mDuration);
            return mDuration;
        }
        Log.i(TAG, "getDuration mDuration3:" + mDuration);
        mDuration = -1;
        return mDuration;
    }

    public void getThumbnailFrame(int position, int number, int interval) {
        if (position != -1) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences("VideoGLSurfaceView", Context.MODE_PRIVATE);
            boolean canSetMultiThumbs = sharedPreferences.getBoolean("SET_MULTI_THUMBS", false);
            if (!canSetMultiThumbs) {
                return;
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("SET_MULTI_THUMBS", false);
            editor.commit();
        }
        Log.i(TAG, "============= getThumbnailFrame position:" + position + " number:" + number + " interval:" + interval);
        mSeekPosition = position;
        mThumbnailNumber = number;
        mThumbnailInterval = interval;
        Parcel parcel = Parcel.obtain();
        parcel.writeInt(position); // position
        parcel.writeInt(number); // number
        parcel.writeInt(interval); // interval
        if (mThumbnailMMediaPlayer != null) {
            mThumbnailMMediaPlayer.setParameter(KEY_PARAMETER_SET_MULTI_THUMBS, parcel);
        }
        parcel.recycle();
    }

    public void releaseThumbnailPlayer(boolean flag) {
        Log.i(TAG, "releaseThumbnailPlayer -------------- start flag:" + flag);
        doClear();
        queueEvent(new Runnable() {
            public void run() {
                mRenderer.doBreak();
            }
        });
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("VideoGLSurfaceView", Context.MODE_PRIVATE);
        boolean canSetMultiThumbs = sharedPreferences.getBoolean("SET_MULTI_THUMBS", false);
        while (!canSetMultiThumbs) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.i(TAG, "InterruptedException");
            }
            canSetMultiThumbs = sharedPreferences.getBoolean("SET_MULTI_THUMBS", false);
        }
        if (flag) {
            if (mThumbnailMMediaPlayer != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mThumbnailMMediaPlayer.isPlaying()) {
                            Log.i(TAG, "mThumbnailMMediaPlayer.isPlaying");
                            mCurrentState = MEDIA_PLAYER_IDEL;
                            mThumbnailMMediaPlayer.stop();
                            mThumbnailMMediaPlayer.release();
                            mThumbnailMMediaPlayer = null;
                        } else {
                            Log.i(TAG, "!mThumbnailMMediaPlayer.isPlaying");
                            mCurrentState = MEDIA_PLAYER_IDEL;
                            mThumbnailMMediaPlayer.stop();
                            mThumbnailMMediaPlayer.release();
                            mThumbnailMMediaPlayer = null;
                        }
                    }
                }).start();
            }
        } else {
            if (mThumbnailMMediaPlayer != null) {
                mCurrentState = MEDIA_PLAYER_IDEL;
                mThumbnailMMediaPlayer.stop();
                mThumbnailMMediaPlayer.release();
                mThumbnailMMediaPlayer = null;
            }
        }
        Log.i(TAG, "releaseThumbnailPlayer -------------- end");
    }

    /**
     * A renderer to read each video frame from a media player,
     * draw it over a surface texture.
     */
    private static class VideoDumpRenderer implements Renderer {
        private static final String TAG = "VideoGLSurfaceView-VideoDumpRenderer";
        private static final int FLOAT_SIZE_BYTES = 4;
        private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
        private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
        private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
        private static final int BORDER_WIDTH = 2;
        private final float[] mTriangleVerticesData = {
                // X, Y, Z, U, V
                -1.0f, -1.0f, 0, 0.f, 0.f,
                1.0f, -1.0f, 0, 1.f, 0.f,
                -1.0f, 1.0f, 0, 0.f, 1.f,
                1.0f, 1.0f, 0, 1.f, 1.f,
        };

        private FloatBuffer mTriangleVertices;

        private final String mVertexShader =
                // This matrix member variable provides a hook to manipulate
                // the coordinates of the objects that use this vertex shader
                "uniform mat4 uMVPMatrix;\n" +              //总变换矩阵
                        "attribute vec4 aPosition;\n" +
                        "attribute vec2 aTextureCoord;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "void main() {\n" +
                        // the matrix must be included as a modifier of gl_Position
                        "  gl_Position = uMVPMatrix * aPosition;\n" +
                        "  vTextureCoord = aTextureCoord;\n" +
                        "}\n";

        private final String mFragmentShader =
                "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform sampler2D sTexture;\n" +
                        "void main() {\n" +
                        "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                        "  gl_FragColor.a = 1.0;\n" +
                        "}\n";

        private float[] mMVPMatrix = new float[16];
        private float[] mSTMatrix = new float[16];

        private int mProgram;
        private int mTextureID = 1;
        private int muMVPMatrixHandle;
        private int muSTMatrixHandle;
        private int maPositionHandle;
        private int maTextureHandle;
        // RTT(Render to Texture)
        private final String mVertexShaderRTT =
                "uniform mat4 uSTMatrix;\n" +
                        "attribute vec4 aPosition;\n" +
                        "attribute vec4 aTextureCoord;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "void main() {\n" +
                        "  gl_Position = aPosition;\n" +
                        "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                        "}\n";

        private final String mFragmentShaderRTT =
                "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                        "}\n";

        private int mProgramRTT;
        private int maPositionHandleRTT;
        private int maTextureHandleRTT;

        private Context mContext;
        private Handler mHandler;
        private SurfaceTexture mSurfaceTexture;
        private boolean updateSurface;
        private boolean isClear;
        private int mFrameNumber;
        private int count;

        // Magic key
        private ByteBuffer mBuffer = null;
        private boolean mDrawFrame;

        private long mCurrentTimestamp = 0;

        private static final int NUM_THUMBNAILS = 6;
        private int[] mTextures = new int[NUM_THUMBNAILS];
        private int[] mFramebuffers = new int[NUM_THUMBNAILS];
        private boolean isFrambufferInit = false;
        private boolean mNeedBreak = false;

        private void setupFramebuffer() {
            if (isFrambufferInit)
                return;
            SharedPreferences sharedPreferences = mContext.getSharedPreferences("VideoGLSurfaceView", Context.MODE_PRIVATE);
            int surfaceWidth = sharedPreferences.getInt("SurfaceWidth", 273);  // on FullHD Panel SurfaceWidth default is 273
            int surfaceHeight = sharedPreferences.getInt("SurfaceHeight", 175); // on FullHD Panel SurfaceHeight default is 175

            // Generate Texture ID
            GLES20.glGenTextures(NUM_THUMBNAILS, mTextures, 0);
            checkGlError("glGenTextures");
            // Generate Texture Buffer
            GLES20.glGenFramebuffers(NUM_THUMBNAILS, mFramebuffers, 0);
            checkGlError("glGenFramebuffers");

            for (int i = 0; i < NUM_THUMBNAILS; ++i) {
                // Bind Texture
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[i]);
                checkGlError("glBindTexture");
                //  void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels)
                // level & border default  = 0
                // or use function void texImage2D (int target, int level, Bitmap bitmap, int border) instead.
                // GLUtils.texImage2D (GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, surfaceWidth, surfaceHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
                checkGlError("glTexImage2D");
                // set filter
                // void glTexParameterx (int target, int pname, int param)
                // pname set GL_TEXTURE_MAG_FILTER & GL_TEXTURE_MIN_FILTER
                // param set GL_LINEAR(need CPU & GPU do more operation) or GL_NEAREST
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                checkGlError("glTexParameteri GL_TEXTURE_MIN_FILTER");
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
                checkGlError("glTexParameteri GL_TEXTURE_MAG_FILTER");
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                checkGlError("glBindTexture");

                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffers[i]);
                checkGlError("glBindFramebuffer");
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextures[i], 0);
                checkGlError("glFramebufferTexture2D");
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                checkGlError("glBindFramebuffer");
            }

            isFrambufferInit = true;
        }

        private void renderToTexture(int index) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences("VideoGLSurfaceView", Context.MODE_PRIVATE);
            int surfaceWidth = sharedPreferences.getInt("SurfaceWidth", 273);
            int surfaceHeight = sharedPreferences.getInt("SurfaceHeight", 175);

            if (index < 0) index = 0;
            else if (index >= NUM_THUMBNAILS) index = NUM_THUMBNAILS - 1;

            // Load the program, which is the basics rules to draw the vertexes and textures.
            // Add program to OpenGL environment
            GLES20.glUseProgram(mProgramRTT);
            checkGlError("glUseProgram");

            GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
            checkGlError("glUniformMatrix4fv mSTMatrix");

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffers[index]);
            checkGlError("glBindFramebuffer");
            GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
            checkGlError("glViewport");
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            checkGlError("glClear");
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            checkGlError("glDrawArrays");
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            checkGlError("glBindFramebuffer");
            GLES20.glFinish();
            checkGlError("glFinish");
            GLES20.glUseProgram(mProgram);
            checkGlError("mProgram");
        }

        public VideoDumpRenderer(Context context) {
            Log.i(TAG, "VideoDumpRenderer ---------------------- begin");
            mContext = context;
            mTriangleVertices = ByteBuffer.allocateDirect(
                    mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mTriangleVertices.put(mTriangleVerticesData).position(0);

            Matrix.setIdentityM(mSTMatrix, 0);
            mSTMatrix[5] = -1.0f;
            mSTMatrix[13] = 1.0f;
        }

        public void setContext(Context context) {
            Log.i(TAG, "setContext ------------------- begin context:" + context);
            mContext = context;
        }

        public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
            Log.i(TAG, "setSurfaceTexture ------------------- begin surfaceTexture:" + surfaceTexture);
            mSurfaceTexture = surfaceTexture;
            mFrameNumber = 0;
            mSurfaceTexture.setOnFrameAvailableListener(VideoFrameAvaliableListener);
        }

        public void setHandler(Handler handler) {
            mHandler = handler;
        }

        public void doClear() {
            // Log.i(TAG, "========== GLSurfaceView.Renderer doClear ===========");
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); //transluent
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            Message msg = new Message();
            msg.what = Constants.HideThumbnailBorderView;
            if (mHandler != null) {
                mHandler.sendMessageDelayed(msg, 500);
            }
        }

        public void doBreak() {
            mNeedBreak = true;
        }

        private void initialize() {
            // Load the program, which is the basics rules to draw the vertexes and textures.
            // Add program to OpenGL environment
            GLES20.glUseProgram(mProgramRTT);
            checkGlError("glUseProgram");

            // Load the vertexes coordinates. Simple here since it only draw a rectangle
            // that fits the whole screen.
            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
            GLES20.glVertexAttribPointer(maPositionHandleRTT, 3, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            checkGlError("glVertexAttribPointer maPosition");
            GLES20.glEnableVertexAttribArray(maPositionHandleRTT);
            checkGlError("glEnableVertexAttribArray maPositionHandleRTT");

            // Load the texture coordinates, which is essentially a rectangle that fits
            // the whole video frame.
            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
            GLES20.glVertexAttribPointer(maTextureHandleRTT, 2, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            checkGlError("glVertexAttribPointer maTextureHandleRTT");

            GLES20.glEnableVertexAttribArray(maTextureHandleRTT);
            checkGlError("glEnableVertexAttribArray maTextureHandleRTT");

            // Load the program, which is the basics rules to draw the vertexes and textures.
            GLES20.glUseProgram(mProgram);
            checkGlError("glUseProgram");

            // Load the vertexes coordinates. Simple here since it only draw a rectangle
            // that fits the whole screen.
            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
            GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            checkGlError("glVertexAttribPointer maPosition");
            GLES20.glEnableVertexAttribArray(maPositionHandle);
            checkGlError("glEnableVertexAttribArray maPositionHandle");

            // Load the texture coordinates, which is essentially a rectangle that fits
            // the whole video frame.
            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
            GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            checkGlError("glVertexAttribPointer maTextureHandle");
            GLES20.glEnableVertexAttribArray(maTextureHandle);
            checkGlError("glEnableVertexAttribArray maTextureHandle");

            // Set up the GL matrices.
            Matrix.setIdentityM(mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            checkGlError("glUniformMatrix4fv mMVPMatrix");

            // Initial clear.
            //  GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); //transluent
            // Draw background color
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        }

        /**
         * Called to draw the current frame.
         * This method is responsible for drawing the current frame.
         */
        public void onDrawFrame(GL10 glUnused) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences("VideoGLSurfaceView", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            boolean seekBarOnHover = sharedPreferences.getBoolean("SeekBarOnHover", false);

            // if (mFrameNumber <= 0 || !seekBarOnHover || mSurfaceTexture == null) {
            if (mFrameNumber <= 0 || mSurfaceTexture == null) {
                boolean thumbnailOnHover = sharedPreferences.getBoolean("ThumbnailOnHover", false);
                if (thumbnailOnHover) {
                    enargeThumbnail();
                    return;
                }
                return;
            }

            initialize();

            int i = 0;
            while (true) {
                Log.i(TAG, "mFrameNumber:" + mFrameNumber + " i:" + i);
                if (mNeedBreak) {
                    mNeedBreak = false;
                    editor.putBoolean("SET_MULTI_THUMBS", true);
                    editor.putBoolean("SeekBarOnHover", false);
                    editor.commit();
                    break;
                }
                synchronized (this) {
                    if (mFrameNumber <= 0) {
                        continue;
                    }
                    mFrameNumber--;
                }

                Log.i(TAG, "=======Start======== mSurfaceTexture.updateTexImage  i:" + i);
                mSurfaceTexture.updateTexImage();
                checkGlError("updateTexImage");
                Log.i(TAG, "=======End======== mSurfaceTexture.updateTexImage  i:" + i);

                long getTimestamp = mSurfaceTexture.getTimestamp();
                editor.putInt("TextureTimeStamp" + i, (int) (getTimestamp / 1000000));
                editor.commit();
                Log.i(TAG, "getTimestamp:" + getTimestamp);
                if (getTimestamp == -1) {
                    Log.i(TAG, "================== getTimestamp: " + getTimestamp + " i:" + i);
                    editor.putBoolean("SET_MULTI_THUMBS", true);
                    editor.putBoolean("SeekBarOnHover", false);
                    editor.commit();
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(Constants.FowrardThumbnail);
                    }
                    return;
                }

                mSurfaceTexture.getTransformMatrix(mSTMatrix);
                renderToTexture(i);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[i]);

                int surfaceWidth = sharedPreferences.getInt("SurfaceWidth", 273);
                int surfaceHeight = sharedPreferences.getInt("SurfaceHeight", 175);
                GLES20.glViewport((surfaceWidth + 15) * i + BORDER_WIDTH, BORDER_WIDTH, surfaceWidth - BORDER_WIDTH, surfaceHeight - BORDER_WIDTH);

                Log.i(TAG, "=======Start======== GLES20.glDrawArrays  i:" + i);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
                checkGlError("glDrawArrays");
                Log.i(TAG, "=======End======== GLES20.glDrawArrays  i:" + i);
                Message msg = new Message();
                msg.what = Constants.ShowThumbnailBorderView;
                msg.arg1 = -1;
                mHandler.sendMessage(msg);
                i++;
            }
        }

        private void enargeThumbnail() {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences("VideoGLSurfaceView", Context.MODE_PRIVATE);
            boolean thumbnailBorderViewFocusFlag = sharedPreferences.getBoolean("ThumbnailBorderViewFocus", false);
            if (!thumbnailBorderViewFocusFlag) {
                return;
            }
            initialize();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            int surfaceWidth = sharedPreferences.getInt("SurfaceWidth", 273);
            int surfaceHeight = sharedPreferences.getInt("SurfaceHeight", 175);
            int index = sharedPreferences.getInt("Index", -1);

            for (int i = 0; i < 5; i++) {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[i]);
                Log.i(TAG, "index:" + index + " i:" + i);
                if (index == i) {
                    GLES20.glViewport((surfaceWidth + 15) * i + BORDER_WIDTH, BORDER_WIDTH, surfaceWidth - BORDER_WIDTH, surfaceHeight + 100 - BORDER_WIDTH);
                } else {
                    GLES20.glViewport((surfaceWidth + 15) * i + BORDER_WIDTH, BORDER_WIDTH, surfaceWidth - BORDER_WIDTH, surfaceHeight - BORDER_WIDTH);
                }
                Log.i(TAG, "=======Start======== GLES20.glDrawArrays  i:" + i);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
                checkGlError("glDrawArrays");
                Log.i(TAG, "=======End======== GLES20.glDrawArrays  i:" + i);
                Message msg = new Message();
                msg.what = Constants.ShowThumbnailBorderView;
                msg.arg1 = index;
                mHandler.sendMessage(msg);
            }
        }

        SurfaceTexture.OnFrameAvailableListener VideoFrameAvaliableListener = new SurfaceTexture.OnFrameAvailableListener() {
            synchronized public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            /* For simplicity, SurfaceTexture calls here when it has new
             * data available.  Call may come in from some random thread,
             * so let's be safe and use synchronize. No OpenGL calls can be done here.s
             */
                mFrameNumber++;
                if (mFrameNumber > 0) {
                    SharedPreferences sharedPreferences = mContext.getSharedPreferences("VideoGLSurfaceView", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("SET_MULTI_THUMBS", false);
                    editor.commit();
                }
                Log.i(TAG, "=============== onFrameAvailable  surfaceTexture:" + surfaceTexture + " mFrameNumber:" + mFrameNumber);
            }
        };

        public void onSurfaceChanged(GL10 glUnused, int width, int height) {
            Log.i(TAG, "onSurfaceChanged -------------------- begin" + "Surface size: width:" + width + " height:" + height);
            GLES20.glViewport(0, 0, 225, 150);
        }

        public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
            Log.d(TAG, "onSurfaceCreated");

            /* Set up shaders and handles to their variables */
            mProgram = createProgram(mVertexShader, mFragmentShader);
            if (mProgram == 0) {
                return;
            }
            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
            checkGlError("glGetAttribLocation aPosition");
            if (maPositionHandle == -1) {
                throw new RuntimeException("Could not get attrib location for aPosition");
            }
            maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
            checkGlError("glGetAttribLocation aTextureCoord");
            if (maTextureHandle == -1) {
                throw new RuntimeException("Could not get attrib location for aTextureCoord");
            }

            muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            checkGlError("glGetUniformLocation uMVPMatrix");
            if (muMVPMatrixHandle == -1) {
                throw new RuntimeException("Could not get attrib location for uMVPMatrix");
            }

            mProgramRTT = createProgram(mVertexShaderRTT, mFragmentShaderRTT);
            if (mProgramRTT == 0) {
                return;
            }
            maPositionHandleRTT = GLES20.glGetAttribLocation(mProgramRTT, "aPosition");
            checkGlError("glGetAttribLocation aPosition");
            if (maPositionHandleRTT == -1) {
                throw new RuntimeException("Could not get attrib location for aPosition");
            }
            maTextureHandleRTT = GLES20.glGetAttribLocation(mProgramRTT, "aTextureCoord");
            checkGlError("glGetAttribLocation aTextureCoord");
            if (maTextureHandleRTT == -1) {
                throw new RuntimeException("Could not get attrib location for aTextureCoord");
            }

            muSTMatrixHandle = GLES20.glGetUniformLocation(mProgramRTT, "uSTMatrix");
            checkGlError("glGetUniformLocation uSTMatrix");
            if (muSTMatrixHandle == -1) {
                throw new RuntimeException("Could not get attrib location for uSTMatrix");
            }

        }

        private int loadShader(int shaderType, String source) {
            int shader = GLES20.glCreateShader(shaderType);
            if (shader != 0) {
                GLES20.glShaderSource(shader, source);
                GLES20.glCompileShader(shader);
                int[] compiled = new int[1];
                GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
                if (compiled[0] == 0) {
                    Log.e(TAG, "Could not compile shader " + shaderType + ":");
                    Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                    GLES20.glDeleteShader(shader);
                    shader = 0;
                }
            }
            return shader;
        }

        private int createProgram(String vertexSource, String fragmentSource) {
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
            if (vertexShader == 0) {
                return 0;
            }
            int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
            if (pixelShader == 0) {
                return 0;
            }

            int program = GLES20.glCreateProgram();
            if (program != 0) {
                GLES20.glAttachShader(program, vertexShader);
                checkGlError("glAttachShader");
                GLES20.glAttachShader(program, pixelShader);
                checkGlError("glAttachShader");
                GLES20.glLinkProgram(program);
                int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
                if (linkStatus[0] != GLES20.GL_TRUE) {
                    Log.e(TAG, "Could not link program: ");
                    Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                    GLES20.glDeleteProgram(program);
                    program = 0;
                }
            }
            return program;
        }

        private void checkGlError(String op) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                Log.e(TAG, op + ": glError " + error);
                throw new RuntimeException(op + ": glError " + error);
            }
        }

    }  // End of class VideoDumpRender.


}  // End of class VideoGLSurfaceView.
