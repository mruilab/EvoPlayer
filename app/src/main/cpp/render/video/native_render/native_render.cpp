//
// Created by mruilab on 2022/4/3.
//

#include "native_render.h"

NativeRender::NativeRender(JNIEnv *env, jobject surface) {
    m_surface_ref = env->NewGlobalRef(surface);
}

NativeRender::~NativeRender() {

}

void NativeRender::InitRender(JNIEnv *env, int video_width, int video_height, int *dts_size) {
    m_native_window = ANativeWindow_fromSurface(env, m_surface_ref);

    //绘制区域的宽高
    int windowWidth = ANativeWindow_getWidth(m_native_window);
    int windowHeight = ANativeWindow_getHeight(m_native_window);

    // 计算目标视频的宽高
    m_dst_w = windowWidth;
    m_dst_h = m_dst_w * video_height / video_width;
    if (m_dst_h > windowHeight) {
        m_dst_h = windowHeight;
        m_dst_w = windowHeight * video_width / video_height;
    }
    LOGI(TAG, "windowW: %d, windowH: %d, dstVideoW: %d, dstVideoH: %d",
         windowWidth, windowHeight, m_dst_w, m_dst_h);

    //设置宽高限制缓冲区中的像素数量
    ANativeWindow_setBuffersGeometry(m_native_window, windowWidth, windowHeight,
                                     WINDOW_FORMAT_RGBA_8888);
    dts_size[0] = m_dst_w;
    dts_size[1] = m_dst_h;
}

void NativeRender::Render(VideoFrame *video_frame) {
    LOGI(TAG, "current render frame id: %d", ++frame_id);
    // 锁定窗口
    ANativeWindow_lock(m_native_window, &m_out_buffer, NULL);
    uint8_t *dst = (uint8_t *) m_out_buffer.bits;
    // 获取stride：一行可以保存的内存像素数量*4（即：rgba的位数）
    int dstStride = m_out_buffer.stride * 4;
    int srcStride = video_frame->frame->linesize[0];

    // 由于window的stride和帧的stride不同，因此需要逐行复制
    for (int h = 0; h < m_dst_h; h++) {
        memcpy(dst + h * dstStride, video_frame->frame->data[0] + h * srcStride, srcStride);
    }
    //释放窗口
    ANativeWindow_unlockAndPost(m_native_window);
}

void NativeRender::ReleaseRender() {
    if (m_native_window != NULL) {
        ANativeWindow_release(m_native_window);
    }
    av_free(&m_out_buffer);
}