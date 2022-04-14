package com.mruilab.evoplayer.beauty;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

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

    private Thread mThread;

    private VideoDecoder mVideoDecoder;
    private GLSurfaceView mGlSurfaceView;
    private YuvRender mRender;

    private String video_path = "/sdcard/av/cheerios9.mp4";

    /*渲染控制器*/
    private FURenderKit mFURenderKit = FURenderKit.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beauty);

        video_path = getIntent().getStringExtra("path");
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
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                initBeauty();
                mVideoDecoder.decode(video_path, new VideoDecoder.DecodeCallback() {
                    @Override
                    public void onDecode(byte[] yuv, int width, int height, int frameCount, long presentationTimeUs) {
                        Log.d(TAG, "length：" + yuv.length + "，width：" + width + "，height：" + height +
                                "，frameCount: " + frameCount + ", presentationTimeUs: " + presentationTimeUs);
                        int faceNum = FUAIKit.getInstance().trackFace(yuv,
                                FUInputBufferEnum.FU_FORMAT_YUV_BUFFER, width, height);
                        Log.d(TAG, "Track Face Number:" + faceNum);
                        dealWithYuv(yuv, width, height);
                    }

                    @Override
                    public void onFinish() {
                        Log.d(TAG, "onFinish");
                    }

                    @Override
                    public void onStop() {
                        Log.d(TAG, "onStop");
                    }
                });
            }
        });

        mThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoDecoder != null) {
            mVideoDecoder.stop();
            mVideoDecoder = null;
        }
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        FUAIKit.getInstance().releaseAllAIProcessor();
        mFURenderKit.releaseEGLContext();
        mFURenderKit.release();
    }

    private void initBeauty() {
        mFURenderKit.createEGLContext();
        FUAIKit.getInstance().loadAIProcessor(DemoConfig.BUNDLE_AI_FACE, FUAITypeEnum.FUAITYPE_FACEPROCESSOR);//加载人脸驱动
        mFURenderKit.setFaceBeauty(FaceBeautySource.getDefaultFaceBeauty());//设置美颜特效
        FUAIKit.getInstance().setMaxFaces(4);//设置最大人脸数
        Log.d(TAG, "isAIProcessorLoaded:" + FUAIKit.getInstance().isAIProcessorLoaded(FUAITypeEnum.FUAITYPE_FACEPROCESSOR));
    }

    private void dealWithYuv(byte[] i420, int width, int height) {
        FURenderInputData inputData = new FURenderInputData(width, height);
        FURenderInputData.FUImageBuffer imageBuffer =
                new FURenderInputData.FUImageBuffer(FUInputBufferEnum.FU_FORMAT_I420_BUFFER, i420);
        FURenderInputData.FURenderConfig config = new FURenderInputData.FURenderConfig();
        config.setNeedBufferReturn(true);
        config.setOutputMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
        inputData.setRenderConfig(config);
        inputData.setImageBuffer(imageBuffer);

        FURenderOutputData outputData = mFURenderKit.renderWithInput(inputData);
        byte[] buffer = outputData.getImage().getBuffer();
        mRender.setYuvData(buffer, width, height);
        mGlSurfaceView.requestRender();
    }
}
