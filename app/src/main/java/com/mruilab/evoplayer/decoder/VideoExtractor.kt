package com.mruilab.evoplayer.decoder

import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

class VideoExtractor {

    var mediaExtractor: MediaExtractor = MediaExtractor()
    var videoTrackId: Int = -1
    lateinit var videoFormat: MediaFormat
    var curSampleTime: Long = 0
    var curSampleFlags: Int = 0

    constructor(videoPath: String) {
        // 设置数据源
        mediaExtractor.setDataSource(videoPath)
        // 获取视频轨道
        for (i in 0 until mediaExtractor.trackCount) {
            val format: MediaFormat = mediaExtractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith("video/")) {
                videoTrackId = i
                videoFormat = format
                break
            }
        }
    }

    fun readBuffer(buffer: ByteBuffer): Int {
        //先清空数据
        buffer.clear()
        //选择要解析的轨道
        mediaExtractor.selectTrack(videoTrackId)
        //读取当前帧的数据
        val sampleSize: Int = mediaExtractor.readSampleData(buffer, 0)
        if (sampleSize < 0) {
            return -1
        }
        //记录当前时间戳
        curSampleTime = mediaExtractor.sampleTime
        //记录当前帧的标志位
        curSampleFlags = mediaExtractor.sampleFlags
        //进入下一帧
        mediaExtractor.advance()
        return sampleSize
    }

    /**
     * 获取视频的sps信息
     */
    fun getSPS(): ByteArray {
        val sps: ByteBuffer = videoFormat.getByteBuffer("csd-0") ?: return ByteArray(0)
        var bytes = ByteArray(sps.capacity())
        sps.get(bytes)
        return bytes
    }

    /**
     * 获取视频的pps信息
     */
    fun getPPS(): ByteArray {
        val sps: ByteBuffer = videoFormat.getByteBuffer("csd-1") ?: return ByteArray(0)
        var bytes = ByteArray(sps.capacity())
        sps.get(bytes)
        return bytes
    }

    /**
     * 获取视频fps
     */
    fun getFps(): Int {
        return videoFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
    }

    /**
     * 释放资源
     */
    fun release() {
        mediaExtractor.release()
    }

}