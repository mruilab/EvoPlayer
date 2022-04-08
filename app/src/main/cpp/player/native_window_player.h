//
// Created by mruilab on 2022/3/31.
//

#ifndef EVOPLAYER_NATIVE_WINDOW_PLAYER_H
#define EVOPLAYER_NATIVE_WINDOW_PLAYER_H

#include <android/native_window.h>
#include <android/native_window_jni.h>

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/frame.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
}

class NativeWindowPlayer {
    const char *TAG = "NativeWindowPlayer";

public:
    void playVideo(const char *input_str, ANativeWindow *nativeWindow);

    void renderFrame();

private:
    //封装格式上下文
    AVFormatContext *m_AVFormatContext = nullptr;
    //解码器上下文
    AVCodecContext *m_AVCodecContext = nullptr;
    //解码器
    AVCodec *m_AVCodec = nullptr;
    //解码的数据包
    AVPacket *m_AVPacket = nullptr;
    //解码的帧
    AVFrame *m_AVFrame = nullptr;
    //数据流索引
    int m_StreamIndex = -1;

    int m_VideoWidth = 0;
    int m_VideoHeight = 0;

    AVFrame *m_RGBFrame = nullptr;
    SwsContext *m_SwsContext = nullptr;
    uint8_t *m_FrameBuffer = nullptr;
    ANativeWindow *m_NativeWindow;
    ANativeWindow_Buffer m_NativeWindowBuffer;
    // 开始解码的时间戳，用于计算解码耗时
    int64_t startDecodeTime;


};

#endif //EVOPLAYER_NATIVE_WINDOW_PLAYER_H
