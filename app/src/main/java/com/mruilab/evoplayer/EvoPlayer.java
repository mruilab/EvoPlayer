package com.mruilab.evoplayer;

import android.view.Surface;

public class EvoPlayer {
    static {
        System.loadLibrary("evo-lib");
    }

    public static native String getFFmpegVersion();

    public native int playVideo(String videoPath, Surface surface);


}