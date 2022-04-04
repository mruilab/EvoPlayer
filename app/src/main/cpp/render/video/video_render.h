//
// Created by mruilab on 2022/4/3.
//

#ifndef EVOPLAYER_VIDEO_RENDER_H
#define EVOPLAYER_VIDEO_RENDER_H

#include <jni.h>

#include "one_frame.h"

class VideoRender {
public:
    virtual void InitRender(JNIEnv *env, int video_width, int video_height, int *dts_size) = 0;

    virtual void Render(OneFrame *one_frame) = 0;

    virtual void ReleaseRender() = 0;
};

#endif //EVOPLAYER_VIDEO_RENDER_H
