package com.mruilab.evoplayer.beauty;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beauty);
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

        playVideo();
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


    private void initBeauty() {
        FUAIKit.getInstance().loadAIProcessor(DemoConfig.BUNDLE_AI_FACE, FUAITypeEnum.FUAITYPE_FACEPROCESSOR);//加载人脸驱动
        mFaceBeauty = FaceBeautySource.getDefaultFaceBeauty();
        mFURenderKit.setFaceBeauty(mFaceBeauty);//设置美颜特效
        FUAIKit.getInstance().setMaxFaces(4);//设置最大人脸数

        mConfig = new FURenderInputData.FURenderConfig();
        mConfig.setNeedBufferReturn(true);
        mConfig.setOutputMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
    }

    private void dealWithYuv(byte[] i420, int width, int height) {
        long startMs = System.currentTimeMillis();
        FURenderInputData inputData = new FURenderInputData(width, height);
        FURenderInputData.FUImageBuffer imageBuffer =
                new FURenderInputData.FUImageBuffer(FUInputBufferEnum.FU_FORMAT_I420_BUFFER, i420);

        inputData.setRenderConfig(mConfig);
        inputData.setImageBuffer(imageBuffer);

        FURenderOutputData outputData = mFURenderKit.renderWithInput(inputData);
        byte[] buffer = outputData.getImage().getBuffer();
        Log.i(TAG, "deal with yuv time:" + (System.currentTimeMillis() - startMs));
        mRender.setYuvData(buffer, width, height);
        mGlSurfaceView.requestRender();
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
}
