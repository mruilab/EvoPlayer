package com.mruilab.evoplayer

import android.view.Surface

class EvoPlayer {

    external fun getFFmpegVersion(): String

    external fun getCodecSupport()

    external fun createGLPlayer(videoPath: String, surface: Surface): Long

    external fun playOrPause(player: Long);

    companion object {
        init {
            System.loadLibrary("evo-lib")
        }
    }
}