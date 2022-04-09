package com.mruilab.evoplayer

import android.view.Surface

class EvoPlayer {

    external fun getFFmpegVersion(): String

    external fun getCodecSupport()

    external fun playVideo(videoPath: String, surface: Surface): Int

    external fun createPlayer(videoPath: String, surface: Surface): Long

    external fun play(player: Long);

    external fun createGLPlayer(videoPath: String, surface: Surface): Long

    external fun playOrPause(player: Long);

    external fun stop(player: Long);

    companion object {
        init {
            System.loadLibrary("evo-lib")
        }
    }
}