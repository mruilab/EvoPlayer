package com.mruilab.evoplayer

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import com.mruilab.evoplayer.decoder.DecodeCallback
import com.mruilab.evoplayer.decoder.VideoDecoder
import com.mruilab.evoplayer.render.GLRender
import com.mruilab.evoplayer.utils.ColorFormat
import com.mruilab.evoplayer.utils.Constants

class HWPlayerActivity : Activity(), DecodeCallback {
    private val TAG = HWPlayerActivity::class.java.simpleName

    private var mVideoPath = Constants.DEFAULT_VIDEO_PATH

    lateinit var mGlSurfaceView: GLSurfaceView

    private lateinit var mVideoDecoder: VideoDecoder
    private lateinit var mRender: GLRender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_gl_surface)

        var videoPath = intent.getStringExtra("video_path")
        if (videoPath != null && videoPath.isNotEmpty()) {
            mVideoPath = videoPath
        }

        mVideoDecoder = VideoDecoder()

        if (checkOpenGLES30()) {
            mGlSurfaceView = findViewById(R.id.surface_view)
            mGlSurfaceView.setEGLContextClientVersion(3)
            mRender = GLRender(this)
            mGlSurfaceView.setRenderer(mRender)
            mGlSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }

        Thread { mVideoDecoder.decode(mVideoPath, this) }.start()
    }

    override fun onDecode(
        yuv: ByteArray, width: Int, height: Int, colorFormat: ColorFormat,
        frameId: Int, presentationTimeUs: Long
    ) {
        Log.d(
            TAG, "width：$width,height：$height,format：$colorFormat,frameCount: $frameId, " +
                    "presentationTimeUs: $presentationTimeUs"
        )
        mRender.setYUVData(yuv, width, height, colorFormat)
        mGlSurfaceView.requestRender()
    }

    override fun onDecodeFinish() {
        Log.d(TAG, "onDecodeFinish")
    }

    override fun onDecodeStop() {
        Log.d(TAG, "onDecodeStop")
    }

    private fun checkOpenGLES30(): Boolean {
        val am: ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = am.deviceConfigurationInfo
        Log.i(TAG, "OpenGL ES version:" + info.reqGlEsVersion)
        return (info.reqGlEsVersion >= 0x30000)
    }

}