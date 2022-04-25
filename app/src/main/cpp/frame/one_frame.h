//
// 一帧数据（音频/视频）
// Created by mruilab on 2022/4/2.
//

#ifndef EVOPLAYER_ONE_FRAME_H
#define EVOPLAYER_ONE_FRAME_H


extern "C" {
#include <libavutil/rational.h>
};

class OneFrame {

public:
    int64_t pts;
    AVRational time_base;

    //是否自动回收数据
    bool autoRecycle = true;

    OneFrame(int64_t pts, AVRational time_base, bool autoRecycle = true) {
        this->pts = pts;
        this->time_base = time_base;
        this->autoRecycle = autoRecycle;
    }

    ~OneFrame() {}
};

#endif //EVOPLAYER_ONE_FRAME_H
