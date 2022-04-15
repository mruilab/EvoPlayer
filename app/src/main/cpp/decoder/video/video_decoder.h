//
// Created by mruilab on 2022/4/3.
//

#ifndef EVOPLAYER_VIDEO_DECODER_H
#define EVOPLAYER_VIDEO_DECODER_H

#include "base_decoder.h"
#include "video_render.h"

extern "C" {
#include <libavutil/imgutils.h>
#include <libswscale/swscale.h>
};

class VideoDecoder : public BaseDecoder {
private:
    const char *TAG = "VideoDecoder";

    /**
     * 渲染时用到AVFrame，存放于OneFrame中，
     * 根据DST_FORMAT将yuv转换为目标数据
     */
    AVFrame *m_dst_frame = NULL;

    uint8_t *m_buf_for_dst_frame = NULL;

    //视频格式转换器
    SwsContext *m_sws_ctx = NULL;

    //视频渲染器
    VideoRender *m_video_render = NULL;

    //显示的目标宽
    int m_dst_w;
    //显示的目标高
    int m_dst_h;

    // 用来计算获取目标dst_frame的耗时
    int64_t obtain_dst_frame_time;

    /**
     * 初始化渲染器
     * @param env
     */
    void InitRender(JNIEnv *env);

    /**
     * 初始化显示器
     */
    void InitBuffer();

    /**
     * 初始化视频数据转换器
     */
    void InitSws();

public:
    VideoDecoder(JNIEnv *env, jstring path, bool for_synthesizer = false);

    ~VideoDecoder();

    void SetRender(VideoRender *render);

protected:
    AVMediaType GetMediaType() override {
        return AVMEDIA_TYPE_VIDEO;
    }

    /**
     * 是否需要循环解码
     */
    bool NeedLoopDecode() override;

    /**
     * 准备解码环境
     * 注：在解码线程中回调
     * @param env 解码线程绑定的jni环境
     */
    void Prepare(JNIEnv *env) override;

    /**
     * 渲染
     * 注：在解码线程中回调
     * @param frame 解码RGBA数据
     */
    void Render(AVFrame *frame) override;

    /**
     * 释放回调
     */
    void Release() override;

    const char *const LogSpec() override {
        return "VIDEO";
    }

};


#endif //EVOPLAYER_VIDEO_DECODER_H
