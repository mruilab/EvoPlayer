//
// 音频渲染器定义
// Created by mruilab on 2022/4/25.
//

#ifndef EVOPLAYER_AUDIO_RENDER_H
#define EVOPLAYER_AUDIO_RENDER_H

#include <cstdint>

class AudioRender {
public:
    virtual void InitRender() = 0;

    virtual void Render(uint8_t *pcm, int size) = 0;

    virtual void ReleaseRender() = 0;

    virtual ~AudioRender() {}
};

#endif //EVOPLAYER_AUDIO_RENDER_H
