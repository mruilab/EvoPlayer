package com.mruilab.evoplayer

import android.app.Activity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.mruilab.evoplayer.utils.Constants

class FFmpegPlayerActivity : Activity(), SurfaceHolder.Callback {

    private var mVideoPath: String? = null

    private lateinit var mPlayer: EvoPlayer
    private lateinit var mSurfaceView: SurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_surface)

        val videoPath = intent.getStringExtra(Constants.STRING_EXTRA_VIDEO_PATH)
        if (videoPath != null && videoPath.isNotEmpty()) {
            mVideoPath = videoPath
        }

        mPlayer = EvoPlayer()
        mSurfaceView = findViewById(R.id.surface_view)
        mSurfaceView.holder.addCallback(this)

    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (null != mVideoPath && mVideoPath!!.isNotEmpty()) {
            Thread {
                mPlayer.playVideo(mVideoPath!!, mSurfaceView.holder.surface)
            }.start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }
}