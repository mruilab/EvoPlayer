//
// Created by mruilab on 2022/4/3.
//

#include "video_decoder.h"
#include "image_util.h"

VideoDecoder::VideoDecoder(bool for_synthesizer) : BaseDecoder(for_synthesizer) {
}

VideoDecoder::~VideoDecoder() {
    delete m_video_render;
}

void VideoDecoder::SetRender(VideoRender *render) {
    this->m_video_render = render;
}

void VideoDecoder::Prepare(JNIEnv *env) {
    InitRender(env);
    InitSws();
}

void VideoDecoder::InitRender(JNIEnv *env) {
    if (m_video_render != NULL) {
        int dst_size[2] = {-1, -1};
        m_video_render->InitRender(env, width(), height(), dst_size);

        m_dst_w = dst_size[0];
        m_dst_h = dst_size[1];
        if (m_dst_w == -1) {
            m_dst_w = width();
        }
        if (m_dst_h == -1) {
            m_dst_w = height();
        }
        LOG_INFO(TAG, LogSpec(), "dst %d, %d", m_dst_w, m_dst_h);
    } else {
        LOG_ERROR(TAG, LogSpec(), "Init render error, you should call SetRender first!");
    }
}

void VideoDecoder::InitBuffer(AVPixelFormat format) {
    m_dst_frame = av_frame_alloc();
    // 获取缓存大小
    int numBytes = av_image_get_buffer_size(format, m_dst_w, m_dst_h, 1);
    // 分配内存
    m_buf_for_dst_frame = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
    // 将内存分配给dst_frame，并将内存格式化为三个通道后，分别保存其地址
    av_image_fill_arrays(m_dst_frame->data, m_dst_frame->linesize,
                         m_buf_for_dst_frame, format, m_dst_w, m_dst_h, 1);
}

void VideoDecoder::InitSws() {
    // 初始化格式转换工具
    m_sws_ctx = sws_getContext(width(), height(), video_pixel_format(),
                               m_dst_w, m_dst_h, AV_PIX_FMT_RGBA,
                               SWS_FAST_BILINEAR, NULL, NULL, NULL);
}

void VideoDecoder::Render(AVFrame *frame) {
    if (m_dst_frame == NULL) {
        if (frame->format == AV_PIX_FMT_NV12 ||
            frame->format == AV_PIX_FMT_NV21)
            InitBuffer(AV_PIX_FMT_NV12);
        else if (frame->format == AV_PIX_FMT_YUV420P) {
            InitBuffer(AV_PIX_FMT_YUV420P);
        } else {
            InitBuffer(AV_PIX_FMT_RGBA);
        }
    }
    obtain_dst_frame_time = GetCurMsTime();
    switch (frame->format) {
        case AV_PIX_FMT_YUV420P:
            obtainYUV420p(frame, m_dst_frame);
            m_dst_frame->format = AV_PIX_FMT_YUV420P;
            break;
        case AV_PIX_FMT_NV12:
            obtainNV12orNV21(frame, m_dst_frame);
            m_dst_frame->format = AV_PIX_FMT_NV12;
            break;
        case AV_PIX_FMT_NV21:
            obtainNV12orNV21(frame, m_dst_frame);
            m_dst_frame->format = AV_PIX_FMT_NV21;
            break;
        default:
            sws_scale(m_sws_ctx, frame->data, frame->linesize, 0,
                      height(), m_dst_frame->data, m_dst_frame->linesize);
            m_dst_frame->format = AV_PIX_FMT_RGBA;
            break;
    }
    // YUV420P->0, NV12->23, NV21->24, RGBA->26
    LOG_INFO(TAG, LogSpec(), "obtain dst_frame time: %ldms, src format: %d, dst format: %d",
             GetCurMsTime() - obtain_dst_frame_time, frame->format, m_dst_frame->format)
    VideoFrame *video_frame = new VideoFrame(m_dst_frame, frame->pts, time_base(), false);
    m_video_render->Render(video_frame);

    if (m_state_cb != NULL) {
        if (m_state_cb->DecodeOneFrame(this, video_frame)) {
            Wait(0, 200);
        }
    }
    LOGI(TAG, "decode frame id: %d timestamp: %ld", ++m_frame_id, frame->best_effort_timestamp);
}

bool VideoDecoder::NeedLoopDecode() {
    return true;
}

void VideoDecoder::Release() {
    LOG_INFO(TAG, LogSpec(), "[VIDEO] release")
    m_frame_id = 0;
    if (m_buf_for_dst_frame != NULL) {
        free(m_buf_for_dst_frame);
        m_buf_for_dst_frame = NULL;
    }
    if (m_sws_ctx != NULL) {
        sws_freeContext(m_sws_ctx);
        m_sws_ctx = NULL;
    }
    if (m_video_render != NULL) {
        m_video_render->ReleaseRender();
        m_video_render = NULL;
    }
}