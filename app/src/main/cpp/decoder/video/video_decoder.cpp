//
// Created by mruilab on 2022/4/3.
//

#include "video_decoder.h"
#include "timer.h"
#include "image_util.h"

VideoDecoder::VideoDecoder(JNIEnv *env, jstring path, bool for_synthesizer)
        : BaseDecoder(env, path, for_synthesizer) {
}

VideoDecoder::~VideoDecoder() noexcept {
    delete m_video_render;
}

void VideoDecoder::SetRender(VideoRender *render) {
    this->m_video_render = render;
}

void VideoDecoder::Prepare(JNIEnv *env) {
    InitRender(env);
    InitBuffer();
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

void VideoDecoder::InitBuffer() {
    m_dst_frame = av_frame_alloc();
    m_dst_frame->format = DST_FORMAT;
    // 获取缓存大小
    int numBytes = av_image_get_buffer_size(DST_FORMAT, m_dst_w, m_dst_h, 1);
    // 分配内存
    m_buf_for_dst_frame = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
    // 将内存分配给dst_frame，并将内存格式化为三个通道后，分别保存其地址
    av_image_fill_arrays(m_dst_frame->data, m_dst_frame->linesize,
                         m_buf_for_dst_frame, DST_FORMAT, m_dst_w, m_dst_h, 1);
}

void VideoDecoder::InitSws() {
    // 初始化格式转换工具
    m_sws_ctx = sws_getContext(width(), height(), video_pixel_format(),
                               m_dst_w, m_dst_h, DST_FORMAT,
                               SWS_FAST_BILINEAR, NULL, NULL, NULL);
}

void VideoDecoder::Render(AVFrame *frame) {
    obtain_dst_frame_time = GetCurMsTime();
    if (frame->format != m_dst_frame->format) {
        sws_scale(m_sws_ctx, frame->data, frame->linesize, 0,
                  height(), m_dst_frame->data, m_dst_frame->linesize);
    } else {
        if (frame->format == AV_PIX_FMT_YUV420P)
            obtainYUV420p(frame, m_dst_frame);
        else
            m_dst_frame = frame;
    }
    LOG_INFO(TAG, LogSpec(), "obtain dst_frame time: %ldms",
             GetCurMsTime() - obtain_dst_frame_time)
    OneFrame *one_frame = new OneFrame(m_dst_frame, frame->pts, time_base(), NULL, false);
    m_video_render->Render(one_frame);

    if (m_state_cb != NULL) {
        if (m_state_cb->DecodeOneFrame(this, one_frame)) {
            Wait(0, 200);
        }
    }
}

bool VideoDecoder::NeedLoopDecode() {
    return true;
}

void VideoDecoder::Release() {
    LOG_INFO(TAG, LogSpec(), "[VIDEO] release")
    if (m_dst_frame != NULL) {
        av_frame_free(&m_dst_frame);
        m_dst_frame = NULL;
    }
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