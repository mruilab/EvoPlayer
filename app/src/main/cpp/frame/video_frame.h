//
// 一帧视频数据
// Created by mruilab on 2022/4/25.
//

#ifndef EVOPLAYER_VIDEO_FRAME_H
#define EVOPLAYER_VIDEO_FRAME_H

#include "one_frame.h"

extern "C" {
#include <libavutil/frame.h>
};

class VideoFrame : public OneFrame {
public:
    AVFrame *frame = NULL;

    VideoFrame(AVFrame *frame, int64_t pts, AVRational timeBase, bool autoRecycle)
            : OneFrame(pts, timeBase, autoRecycle) {
        this->frame = frame;
    }

    ~VideoFrame() {
        if (autoRecycle) {
            if (frame != NULL) {
                av_frame_free(&frame);
                frame = NULL;
            }
        }
    }
};

#endif //EVOPLAYER_VIDEO_FRAME_H
