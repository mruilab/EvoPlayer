package com.mruilab.evoplayer.beauty;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.mruilab.evoplayer.R;

public class BeautyActivity extends AppCompatActivity {
    private static final String TAG = BeautyActivity.class.getSimpleName();

    private Thread mThread;

    private VideoDecoder mVideoDecoder;
    private GLSurfaceView mGlSurfaceView;
    private YuvRender mRender;

    private String video_path = "/sdcard/av/cheerios.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beauty);

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
        mThread = new Thread(() -> mVideoDecoder.decode(video_path, new VideoDecoder.DecodeCallback() {
            @Override
            public void onDecode(byte[] yuv, int width, int height, int frameCount, long presentationTimeUs) {
                Log.d("VideoDecoder", "width：" + width + "，height：" + height +
                        "，frameCount: " + frameCount + ", presentationTimeUs: " + presentationTimeUs);
                mRender.setYuvData(yuv, width, height);
                mGlSurfaceView.requestRender();
            }

            @Override
            public void onFinish() {
                Log.d("VideoDecoder", "onFinish");
            }

            @Override
            public void onStop() {
                Log.d("VideoDecoder", "onStop");
            }
        }));
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
    }
}
