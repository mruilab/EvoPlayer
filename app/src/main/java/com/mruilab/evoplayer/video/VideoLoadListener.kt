package com.mruilab.evoplayer.video

interface VideoLoadListener {

    fun onVideoLoaded(videoItems: List<VideoItem>)

    fun onFailed(e: Exception)
}