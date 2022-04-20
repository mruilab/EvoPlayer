package com.mruilab.evoplayer.beauty;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.faceunity.core.entity.FURenderInputData;
import com.faceunity.core.entity.FURenderOutputData;
import com.faceunity.core.enumeration.FUAITypeEnum;
import com.faceunity.core.enumeration.FUInputBufferEnum;
import com.faceunity.core.enumeration.FUTransformMatrixEnum;
import com.faceunity.core.faceunity.FUAIKit;
import com.faceunity.core.faceunity.FURenderKit;
import com.faceunity.core.model.facebeauty.FaceBeauty;
import com.mruilab.evoplayer.R;

public class BeautyActivity extends Activity {
    private static final String TAG = BeautyActivity.class.getSimpleName();

    private TextView mFaceNumText;

    private Thread mThread;

    private VideoDecoder mVideoDecoder;
    private GLSurfaceView mGlSurfaceView;
    private YuvRender mRender;

    private String video_path;

    private FaceBeauty mFaceBeauty;
    private SeekBar sharpen_seek;//锐化
    private SeekBar color_seek; //美白
    private SeekBar red_seek;//红润
    private SeekBar blur_seek;// 磨皮

    /*渲染控制器*/
    private FURenderKit mFURenderKit = FURenderKit.getInstance();
    FURenderInputData.FURenderConfig mConfig;

    private long playerID;

    private RenderBean mRenderBean;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beauty);

        initRenderThread();
        initBeauty();
        video_path = getIntent().getStringExtra("path");

        mFaceNumText = findViewById(R.id.text_face_num);

        sharpen_seek = findViewById(R.id.sharpen_seek);
        color_seek = findViewById(R.id.color_seek);
        red_seek = findViewById(R.id.red_seek);
        blur_seek = findViewById(R.id.blur_seek);

        initSeekBarListener();

        mVideoDecoder = new VideoDecoder();
        mVideoDecoder.setOutputFormat(VideoDecoder.COLOR_FORMAT_I420);

        if (checkOpenGLES30()) {
            mGlSurfaceView = findViewById(R.id.surface_view);
            mGlSurfaceView.setEGLContextClientVersion(3);
            mRender = new YuvRender(this);
            mGlSurfaceView.setRenderer(mRender);
            mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }

//        playVideo();
        if (playerID == 0) {
            playerID = createGLPlayer(video_path, null);
            playOrPause(playerID);
        }
    }

    public void initSeekBarListener() {

        /**
         * 锐化 范围[0.0-1.0]
         */
        sharpen_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFaceBeauty.setSharpenIntensity((double) progress / 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        /**
         * 美白 范围[0.0-1.0]
         */
        color_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //主要是用于监听进度值的改变
                mFaceBeauty.setColorIntensity((double) progress / 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //监听用户开始拖动进度条的时候
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //监听用户结束拖动进度条的时候
            }
        });

        /**
         * 红润 范围[0.0-1.0]
         */
        red_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFaceBeauty.setRedIntensity((double) progress / 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        /**
         * 磨皮 范围[0.0-6.0]
         */
        blur_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFaceBeauty.setBlurIntensity((double) progress / 60);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * 检查设备是否支持OpenGL ES 3.0
     * OpenGL ES 3.0 - 此API 规范受Android 4.3（API 级别18）及更高版本的支持。
     * OpenGL ES 3.1 - 此API 规范受Android 5.0（API 级别21）及更高版本的支持。
     *
     * @return
     */
    private boolean checkOpenGLES30() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        Log.i(TAG, "OpenGL ES version:" + info.reqGlEsVersion);
        return (info.reqGlEsVersion >= 0x30000);
    }

    private void playVideo() {
        mThread = new Thread(() -> {
            mFURenderKit.createEGLContext();
            mFURenderKit.setUseTexAsync(true);
            mVideoDecoder.decode(video_path, new VideoDecoder.DecodeCallback() {
                @Override
                public void onDecode(byte[] yuv, int width, int height, int frameCount, long presentationTimeUs) {
                    Log.d(TAG, "length：" + yuv.length + "，width：" + width + "，height：" + height +
                            "，frameCount: " + frameCount + ", presentationTimeUs: " + presentationTimeUs);
                    int faceNum = FUAIKit.getInstance().trackFace(yuv,
                            FUInputBufferEnum.FU_FORMAT_YUV_BUFFER, width, height);
                    runOnUiThread(() -> {
                        mFaceNumText.setTextColor(faceNum > 0 ? Color.WHITE : Color.RED);
                        mFaceNumText.setText("Track Face Number: " + faceNum);
                    });
                    dealWithYuv(yuv, width, height);
                }

                @Override
                public void onFinish() {
                    mFURenderKit.clearCacheResource();
                    mFURenderKit.releaseEGLContext();
                    mFURenderKit.releaseSafe();
                    Log.d(TAG, "onFinish");
                }

                @Override
                public void onStop() {
                    mFURenderKit.clearCacheResource();
                    mFURenderKit.releaseEGLContext();
                    mFURenderKit.releaseSafe();
                    Log.d(TAG, "onStop");
                }
            });
        });

        mThread.start();
    }

    private void initRenderThread() {
        mHandlerThread = new HandlerThread("RenderThread");
        mHandlerThread.start();

        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        mFURenderKit.createEGLContext();
                        mFURenderKit.setUseTexAsync(true);
                        break;
                    case 2:
                        RenderBean bean = (RenderBean) msg.obj;
                        int faceNum = FUAIKit.getInstance().trackFace(bean.getBuffer(),
                                FUInputBufferEnum.FU_FORMAT_YUV_BUFFER, bean.getWidth(), bean.getHeight());
                        runOnUiThread(() -> {
                            mFaceNumText.setTextColor(faceNum > 0 ? Color.WHITE : Color.RED);
                            mFaceNumText.setText("Track Face Number: " + faceNum);
                        });
                        FURenderInputData inputData = new FURenderInputData(bean.getWidth(), bean.getHeight());
                        FURenderInputData.FUImageBuffer imageBuffer =
                                new FURenderInputData.FUImageBuffer(
                                        FUInputBufferEnum.FU_FORMAT_I420_BUFFER,
                                        bean.getBuffer());

                        inputData.setRenderConfig(mConfig);
                        inputData.setImageBuffer(imageBuffer);

                        FURenderOutputData outputData = mFURenderKit.renderWithInput(inputData);
                        mRender.setYuvData(outputData.getImage().getBuffer(),
                                bean.getWidth(), bean.getHeight(), 0);
                        mGlSurfaceView.requestRender();
                        break;
                    case 3:
                        RenderBean bean1 = (RenderBean) msg.obj;
                        int faceNum1 = FUAIKit.getInstance().trackFace(bean1.getBuffer(),
                                FUInputBufferEnum.FU_FORMAT_YUV_BUFFER, bean1.getWidth(), bean1.getHeight());
                        runOnUiThread(() -> {
                            mFaceNumText.setTextColor(faceNum1 > 0 ? Color.WHITE : Color.RED);
                            mFaceNumText.setText("Track Face Number: " + faceNum1);
                        });
                        FURenderInputData inputData1 = new FURenderInputData(bean1.getWidth(), bean1.getHeight());
                        FURenderInputData.FUImageBuffer imageBuffer1 =
                                new FURenderInputData.FUImageBuffer(
                                        FUInputBufferEnum.FU_FORMAT_NV21_BUFFER,
                                        bean1.getBuffer());

                        inputData1.setRenderConfig(mConfig);
                        inputData1.setImageBuffer(imageBuffer1);

                        FURenderOutputData outputData1 = mFURenderKit.renderWithInput(inputData1);
                        mRender.setYuvData(outputData1.getImage().getBuffer(),
                                bean1.getWidth(), bean1.getHeight(), 1);
                        mGlSurfaceView.requestRender();
                        break;
                    case 4:
                        RenderBean bean2 = (RenderBean) msg.obj;
                        int faceNum2 = FUAIKit.getInstance().trackFace(bean2.getBuffer(),
                                FUInputBufferEnum.FU_FORMAT_YUV_BUFFER, bean2.getWidth(), bean2.getHeight());
                        runOnUiThread(() -> {
                            mFaceNumText.setTextColor(faceNum2 > 0 ? Color.WHITE : Color.RED);
                            mFaceNumText.setText("Track Face Number: " + faceNum2);
                        });
                        FURenderInputData inputData2 = new FURenderInputData(bean2.getWidth(), bean2.getHeight());
                        FURenderInputData.FUImageBuffer imageBuffer2 =
                                new FURenderInputData.FUImageBuffer(
                                        FUInputBufferEnum.FU_FORMAT_NV21_BUFFER,
                                        bean2.getBuffer());

                        inputData2.setRenderConfig(mConfig);
                        inputData2.setImageBuffer(imageBuffer2);

                        FURenderOutputData outputData2 = mFURenderKit.renderWithInput(inputData2);
                        mRender.setYuvData(outputData2.getImage().getBuffer(),
                                bean2.getWidth(), bean2.getHeight(), 2);
                        mGlSurfaceView.requestRender();
                        break;
                    case 6:
                        mFURenderKit.clearCacheResource();
                        mFURenderKit.releaseEGLContext();
                        mFURenderKit.releaseSafe();
                        break;
                }
            }
        };
    }

    private void initBeauty() {
        FUAIKit.getInstance().loadAIProcessor(DemoConfig.BUNDLE_AI_FACE, FUAITypeEnum.FUAITYPE_FACEPROCESSOR);//加载人脸驱动
        mFaceBeauty = FaceBeautySource.getDefaultFaceBeauty();
        mFURenderKit.setFaceBeauty(mFaceBeauty);//设置美颜特效
        FUAIKit.getInstance().setMaxFaces(4);//设置最大人脸数

        mConfig = new FURenderInputData.FURenderConfig();
        mConfig.setNeedBufferReturn(true);
        mConfig.setOutputMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);

        initEglContext();
    }

    private void dealWithYuv(byte[] i420, int width, int height) {
        long startMs = System.currentTimeMillis();
        FURenderInputData inputData = new FURenderInputData(width, height);
        FURenderInputData.FUImageBuffer imageBuffer =
                new FURenderInputData.FUImageBuffer(FUInputBufferEnum.FU_FORMAT_I420_BUFFER, i420);

        inputData.setRenderConfig(mConfig);
        inputData.setImageBuffer(imageBuffer);

        FURenderOutputData outputData = mFURenderKit.renderWithInput(inputData);
        byte[] yBuffer = outputData.getImage().getBuffer();
        Log.i(TAG, "deal with yuv time:" + (System.currentTimeMillis() - startMs));
        mRender.setYuvData(yBuffer, width, height, 0);
        mGlSurfaceView.requestRender();
    }

    public void initEglContext() {
        Message msg = Message.obtain();
        msg.what = 1;
        mHandler.sendMessage(msg);
    }

    public void releaseEglContext() {
        Message msg = Message.obtain();
        msg.what = 6;
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (playerID != 0)
            playOrPause(playerID);
        releaseEglContext();
        mHandlerThread.quitSafely();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FUAIKit.getInstance().releaseAllAIProcessor();
        if (mVideoDecoder != null) {
            mVideoDecoder.stop();
            mVideoDecoder = null;
        }
    }

    /******************************** native ************************************/
    static {
        System.loadLibrary("evo-lib");
    }

    public native String getFFmpegVersion();

    public native void getCodecSupport();

    public native int playVideo(String videoPath, Surface surface);

    public native long createPlayer(String videoPath, Surface surface);

    public native void play(long player);

    public native long createGLPlayer(String videoPath, Surface surface);

    public native void playOrPause(long player);

    public native void stop(long player);

    /**
     * Native层调用
     *
     * @param y
     * @param u
     * @param v
     * @param width
     * @param height
     */
    public void dealWithI420(byte[] y, byte[] u, byte[] v, int width, int height) {
        byte[] yuv = new byte[y.length + u.length + v.length];
        System.arraycopy(y, 0, yuv, 0, y.length);
        System.arraycopy(u, 0, yuv, y.length, u.length);
        System.arraycopy(v, 0, yuv, y.length + u.length, v.length);
        mRenderBean = new RenderBean(yuv, width, height);
        Message msg = Message.obtain();
        msg.what = 2;
        msg.obj = mRenderBean;
        mHandler.sendMessage(msg);
    }

    /**
     * Native层调用
     *
     * @param y
     * @param uv
     * @param width
     * @param height
     */
    public void dealWithNV12(byte[] y, byte[] uv, int width, int height) {
        byte[] yuv = new byte[y.length + uv.length];
        System.arraycopy(y, 0, yuv, 0, y.length);
        System.arraycopy(uv, 0, yuv, y.length, uv.length);
        mRenderBean = new RenderBean(yuv, width, height);
        Message msg = Message.obtain();
        msg.what = 3;
        msg.obj = mRenderBean;
        mHandler.sendMessage(msg);
    }

    /**
     * Native层调用
     *
     * @param y
     * @param uv
     * @param width
     * @param height
     */
    public void dealWithNV21(byte[] y, byte[] uv, int width, int height) {
        byte[] yuv = new byte[y.length + uv.length];
        System.arraycopy(y, 0, yuv, 0, y.length);
        System.arraycopy(uv, 0, yuv, y.length, uv.length);
        mRenderBean = new RenderBean(yuv, width, height);
        Message msg = Message.obtain();
        msg.what = 4;
        msg.obj = mRenderBean;
        mHandler.sendMessage(msg);
    }

    /**
     * Native层调用
     *
     * @param rgba
     * @param width
     * @param height
     */
    public void dealWithRGBA(byte[] rgba, int width, int height) {
        mRenderBean = new RenderBean(rgba, width, height);
        Message msg = Message.obtain();
        msg.what = 5;
        msg.obj = mRenderBean;
        mHandler.sendMessage(msg);
    }
}
