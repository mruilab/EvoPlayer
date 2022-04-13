package com.mruilab.evoplayer.beauty;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoExtractor {
    MediaExtractor mediaExtractor;
    int videoTrackId = -1;
    MediaFormat videoFormat;
    long curSampleTime;
    int curSampleFlags;

    public VideoExtractor(String videoPath) {
        mediaExtractor = new MediaExtractor();
        try {
            //设置数据源
            mediaExtractor.setDataSource(videoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 获取视频轨道
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            if (format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                videoTrackId = i;
                videoFormat = format;
                Log.i("VideoExtractor", "video format:" + videoFormat.toString());
            }
        }
    }

    int readBuffer(ByteBuffer buffer) {
        //先清空数据
        buffer.clear();
        //选择要解析的轨道
        mediaExtractor.selectTrack(videoTrackId);
        //读取当前帧的数据
        int sampleSize = mediaExtractor.readSampleData(buffer, 0);
        if (sampleSize < 0) {
            return -1;
        }
        //记录当前时间戳
        curSampleTime = mediaExtractor.getSampleTime();
        //记录当前帧的标志位
        curSampleFlags = mediaExtractor.getSampleFlags();
        //进入下一帧
        mediaExtractor.advance();
        return sampleSize;
    }


    /**
     * 获取视频 MediaFormat
     *
     * @return
     */
    public MediaFormat getVideoFormat() {
        return videoFormat;
    }

    /**
     * 获取当前帧的时间戳
     *
     * @return
     */
    public long getCurSampleTime() {
        return curSampleTime;
    }

    /**
     * 获取当前帧的标志位
     *
     * @return
     */
    public int getCurSampleFlags() {
        return curSampleFlags;
    }

    public int getVideoTrackId(){
        return videoTrackId;
    }

    /**
     * 获取视频的sps信息
     *
     * @return
     */
    public byte[] getSPS() {
        ByteBuffer sps = videoFormat.getByteBuffer("csd-0");
        if (sps == null) return new byte[]{};
        byte[] bytes = new byte[sps.capacity()];
        sps.get(bytes);
        return bytes;
    }

    /**
     * 获取视频的pps信息
     *
     * @return
     */
    public byte[] getPPS() {
        ByteBuffer pps = videoFormat.getByteBuffer("csd-1");
        if (pps == null) return new byte[]{};
        byte[] bytes = new byte[pps.capacity()];
        pps.get(bytes);
        return bytes;
    }

    /**
     * 获取视频fps
     *
     * @return
     */
    public int getFps() {
        return videoFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
    }

    /**
     * 释放资源
     */
    public void release() {
        mediaExtractor.release();
    }

}
