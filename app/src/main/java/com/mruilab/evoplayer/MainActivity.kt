package com.mruilab.evoplayer

import android.Manifest
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity() {
    lateinit var mSurfaceView: SurfaceView
    lateinit var mPlayer: EvoPlayer
    private var player: Long? = null

    private val RC_READ_EXTERNAL_STORAGE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()

        mPlayer = EvoPlayer()
        val textView: TextView = findViewById(R.id.ffmpeg_version_text)
        textView.movementMethod = ScrollingMovementMethod.getInstance()
        textView.text = mPlayer.getFFmpegVersion()

        mPlayer.getCodecSupport()

        mSurfaceView = findViewById(R.id.surface_view)
        initSurfaceView()

    }

    private fun initSurfaceView() {
        mSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (player == null) {
                    player = mPlayer.createPlayer("/sdcard/av/video.mp4", holder.surface);
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int, width: Int, height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }

        })
    }

    fun onPlayClick(view: View) {
//        Thread(Runnable {
//            mPlayer.playVideo("/sdcard/av/video.mp4", mSurfaceView.holder.surface)
//        }).start()
        mPlayer.play(player!!)

    }

    private fun checkPermissions() {
        val perms: Array<String> = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!EasyPermissions.hasPermissions(this, *perms)) {
            EasyPermissions.requestPermissions(
                this,
                "是否允许\"EvoPlayer\"访问您设备上的照片、媒体内容和文件？",
                RC_READ_EXTERNAL_STORAGE,
                *perms
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}