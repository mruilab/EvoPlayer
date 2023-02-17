package com.mruilab.evoplayer.loader

interface VideoLoadListener {

    fun onVideoLoaded(videoItems: List<VideoItem>)

    fun onFailed(e: Exception)
}