package com.mruilab.evoplayer

import android.view.Surface

class EvoPlayer {

    external fun getFFmpegVersion(): String

    external fun playVideo(videoPath: String, surface: Surface): Int

    companion object {
        init {
            System.loadLibrary("evo-lib")
        }
    }
}