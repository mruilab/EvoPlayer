//
// 一个数据（音频/视频）帧数据
// Created by mruilab on 2022/4/2.
//

#ifndef EVOPLAYER_ONE_FRAME_H
#define EVOPLAYER_ONE_FRAME_H

#include <malloc.h>

extern "C" {
#include <libavutil/frame.h>
#include <libavutil/rational.h>
};

class OneFrame {

public:
    AVFrame *frame = NULL;
    int64_t pts;
    AVRational time_base;
    uint8_t *ext_data = NULL;

    //是否自动回收data和ext_data
    bool autoRecycle = true;

    OneFrame(AVFrame *frame, int64_t pts, AVRational time_base,
             uint8_t *ext_data = NULL, bool autoRecycle = true) {
        this->frame = frame;
        this->pts = pts;
        this->time_base = time_base;
        this->ext_data = ext_data;
        this->autoRecycle = autoRecycle;
    }

    ~OneFrame() {
        if (autoRecycle) {
            if (frame != NULL) {
                av_frame_free(&frame);
                frame = NULL;
            }
            if (ext_data != NULL) {
                free(ext_data);
                ext_data = NULL;
            }
        }
    }
};

#endif //EVOPLAYER_ONE_FRAME_H
