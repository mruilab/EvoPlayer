//
// Created by mruilab on 2022/3/31.
//

#include "native_window_player.h"
#include "logger.h"
#include "timer.h"

void NativeWindowPlayer::playVideo(const char *input_str, ANativeWindow *nativeWindow) {
    do {
        m_NativeWindow = nativeWindow;
        //1.创建封装格式上下文
        m_AVFormatContext = avformat_alloc_context();

        //2.打开输入文件，解封装
        char buf[1024];
        int err_code = avformat_open_input(&m_AVFormatContext, input_str, NULL, NULL);
        if (err_code != 0) {
            av_strerror(err_code, buf, 1024);
            LOGE(TAG, "avformat_open_input fail. %s: %d(%s)", input_str,
                 err_code,
                 buf);
            break;
        }

        //3.获取音视频流信息
        if (avformat_find_stream_info(m_AVFormatContext, NULL) < 0) {
            LOGE(TAG, "avformat_find_stream_info fail.");
            break;
        }

        //4.获取音视频流索引
        for (int index = 0; index < m_AVFormatContext->nb_streams; index++) {
            if (m_AVFormatContext->streams[index]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
                m_StreamIndex = index;
                break;
            }
        }

        if (m_StreamIndex == -1) {
            LOGE(TAG, "Fail to find stream index.");
            break;
        }

        //5.获取解码器参数
        AVCodecParameters *codecParameters = m_AVFormatContext->streams[m_StreamIndex]->codecpar;

        //6.根据 codec_id 获取解码器
        m_AVCodec = avcodec_find_decoder(codecParameters->codec_id);
        if (m_AVCodec == nullptr) {
            LOGE(TAG, "avcodec_find_decoder fail.");
            break;
        }

        //7.创建解码器上下文
        m_AVCodecContext = avcodec_alloc_context3(m_AVCodec);
        if (avcodec_parameters_to_context(m_AVCodecContext, codecParameters) != 0) {
            LOGE(TAG, "avcodec_parameters_to_context fail.");
            break;
        }

        //8.打开解码器
        if (avcodec_open2(m_AVCodecContext, m_AVCodec, NULL) < 0) {
            LOGE(TAG, "avcodec_open2 fail.");
            break;
        }

        //分配存储 RGB 图像的 buffer
        m_VideoWidth = m_AVCodecContext->width;
        m_VideoHeight = m_AVCodecContext->height;

        //9.创建存储编码数据和解码数据的结构体
        m_AVPacket = av_packet_alloc();//创建AVPacket存放编码数据
        m_AVFrame = av_frame_alloc();//创建AVFrame存放解码后的数据
        m_RGBFrame = av_frame_alloc();

        //计算 Buffer 的大小
        int bufferSize = av_image_get_buffer_size(AV_PIX_FMT_RGBA, m_VideoWidth, m_VideoHeight, 1);
        //为 m_RGBAFrame 分配空间
        m_FrameBuffer = (uint8_t *) av_malloc(bufferSize * sizeof(uint8_t));
        av_image_fill_arrays(m_RGBFrame->data, m_RGBFrame->linesize, m_FrameBuffer, AV_PIX_FMT_RGBA,
                             m_VideoWidth, m_VideoHeight, 1);
        //获取转换的上下文
        m_SwsContext = sws_getContext(m_VideoWidth, m_VideoHeight, m_AVCodecContext->pix_fmt,
                                      m_VideoWidth, m_VideoHeight, AV_PIX_FMT_RGBA,
                                      SWS_FAST_BILINEAR, NULL, NULL, NULL);
        // 设置渲染区域和输入格式
        if (ANativeWindow_setBuffersGeometry(m_NativeWindow, m_VideoWidth,
                                             m_VideoHeight,
                                             WINDOW_FORMAT_RGBA_8888) < 0) {
            LOGE(TAG, "ANativeWindow_setBuffersGeometry fail.");
            break;
        }

        //10.解码循环
        while (av_read_frame(m_AVFormatContext, m_AVPacket) >= 0) { //读取帧
            if (m_AVPacket->stream_index == m_StreamIndex) {
                startDecodeTime = GetCurMsTime();
                if (avcodec_send_packet(m_AVCodecContext, m_AVPacket) != 0) {//解码视频
                    LOGE(TAG, "avcodec_send_packet fail.");
                    break;
                }
                LOGD(TAG, "decode frame time: %ldms",
                     GetCurMsTime() - startDecodeTime);
                while (avcodec_receive_frame(m_AVCodecContext, m_AVFrame) == 0) {
                    // 获取到m_AVFrame解码数据，在这里进行格式转换，然后进行渲染
                    sws_scale(m_SwsContext, m_AVFrame->data, m_AVFrame->linesize, 0,
                              m_VideoHeight, m_RGBFrame->data, m_RGBFrame->linesize);
                    if (ANativeWindow_lock(m_NativeWindow, &m_NativeWindowBuffer, nullptr) < 0) {
                        LOGE(TAG, "ANativeWindow_lock fail.");
                        break;
                    } else {
                        uint8_t *dstBuffer = static_cast<uint8_t *>(m_NativeWindowBuffer.bits);
                        int srcLineSize = m_RGBFrame->linesize[0];//输入图的步长(一行像素有多少字节)
                        int dstLineSize = m_NativeWindowBuffer.stride * 4;//RGBA 缓冲区步长
                        for (int i = 0; i < m_VideoHeight; ++i) {
                            //一行一行地拷贝图像数据
                            memcpy(dstBuffer + i * dstLineSize, m_FrameBuffer + i * srcLineSize,
                                   srcLineSize);
                        }
                        ANativeWindow_unlockAndPost(m_NativeWindow);
                    }
                }
            }
            av_packet_unref(m_AVPacket); //释放m_AVPacket引用，防止内存泄露
        }
    } while (false);

    //11.释放资源，解码完成
    if (m_AVFrame != nullptr) {
        av_frame_free(&m_AVFrame);
        m_AVFrame = nullptr;
    }

    if (m_AVPacket != nullptr) {
        av_packet_free(&m_AVPacket);
        m_AVPacket = nullptr;
    }

    if (m_AVCodecContext != nullptr) {
        avcodec_close(m_AVCodecContext);
        avcodec_free_context(&m_AVCodecContext);
        m_AVCodecContext = nullptr;
        m_AVCodec = nullptr;
    }

    if (m_AVFormatContext != nullptr) {
        avformat_close_input(&m_AVFormatContext);
        avformat_free_context(m_AVFormatContext);
        m_AVFormatContext = nullptr;
    }

    if (m_RGBFrame != nullptr) {
        av_frame_free(&m_RGBFrame);
        m_RGBFrame = nullptr;
    }

    if (m_FrameBuffer != nullptr) {
        free(m_FrameBuffer);
        m_FrameBuffer = nullptr;
    }

    if (m_SwsContext != nullptr) {
        sws_freeContext(m_SwsContext);
        m_SwsContext = nullptr;
    }

    if (m_NativeWindow != nullptr)
        ANativeWindow_release(m_NativeWindow);
}