package com.mruilab.evoplayer

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.mruilab.evoplayer.utils.Constants

class FFGLPlayerActivity : Activity(), SurfaceHolder.Callback {
    private val TAG = FFGLPlayerActivity::class.java.simpleName

    private var mVideoPath = Constants.DEFAULT_VIDEO_PATH

    private lateinit var mPlayer: EvoPlayer
    private var mPlayerID: Long? = null
    private lateinit var mSurfaceView: SurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_surface)
        var videoPath = intent.getStringExtra("video_path")
        if (videoPath != null && videoPath.isNotEmpty()) {
            mVideoPath = videoPath
        }

        mPlayer = EvoPlayer()
        mSurfaceView = findViewById(R.id.surface_view)
        mSurfaceView.holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (mPlayerID == null) {
            mPlayerID = mPlayer.createGLPlayer(mVideoPath, holder.surface)
            mPlayer.playOrPause(mPlayerID!!)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.i(TAG, "surfaceChanged width:$width height:$height")
        if (mPlayerID != null) {
            mPlayer.setSurfaceSize(mPlayerID!!, width, height)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (mPlayerID != null) {
            mPlayer.stop(mPlayerID!!)
        }
    }

}