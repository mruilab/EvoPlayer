package com.mruilab.evoplayer

import android.view.Surface

class EvoPlayer {

    external fun getFFmpegVersion(): String

    external fun playVideo(videoPath: String, surface: Surface): Int

    external fun createPlayer(videoPath: String, surface: Surface): Int

    external fun play(player: Int);

    companion object {
        init {
            System.loadLibrary("evo-lib")
        }
    }
}