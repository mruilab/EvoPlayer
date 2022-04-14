package com.mruilab.evoplayer.beauty;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.faceunity.core.entity.FURenderInputData;
import com.faceunity.core.entity.FURenderOutputData;
import com.faceunity.core.enumeration.FUAITypeEnum;
import com.faceunity.core.enumeration.FUInputBufferEnum;
import com.faceunity.core.enumeration.FUTransformMatrixEnum;
import com.faceunity.core.faceunity.FUAIKit;
import com.faceunity.core.faceunity.FURenderKit;
import com.mruilab.evoplayer.R;

public class BeautyActivity extends Activity {
    private static final String TAG = BeautyActivity.class.getSimpleName();

    private TextView mFaceNumText;

    private Thread mThread;

    private VideoDecoder mVideoDecoder;
    private GLSurfaceView mGlSurfaceView;
    private YuvRender mRender;

    private String video_path;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FUAIKit.getInstance().releaseAllAIProcessor();
        if (mVideoDecoder != null) {
            mVideoDecoder.stop();
            mVideoDecoder = null;
        }
    }

    private void initBeauty() {
        FUAIKit.getInstance().loadAIProcessor(DemoConfig.BUNDLE_AI_FACE, FUAITypeEnum.FUAITYPE_FACEPROCESSOR);//加载人脸驱动
        mFURenderKit.setFaceBeauty(FaceBeautySource.getDefaultFaceBeauty());//设置美颜特效
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
}
