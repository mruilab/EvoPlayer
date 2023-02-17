//
// 一帧音频数据
// Created by mruilab on 2022/4/25.
//

#ifndef EVOPLAYER_AUDIO_FRAME_H
#define EVOPLAYER_AUDIO_FRAME_H

#include <malloc.h>
#include "one_frame.h"

class AudioFrame : public OneFrame {
public:

    uint8_t *pcm = NULL;
    int pcm_size;
    uint8_t *ext_data = NULL;

    AudioFrame(uint8_t *pcm, int pcm_size, uint8_t *ext_data, int64_t pts, AVRational timeBase,
               bool autoRecycle) : OneFrame(pts, timeBase, autoRecycle) {
        this->pcm = pcm;
        this->pcm_size = pcm_size;
        this->ext_data = ext_data;
    }

    ~AudioFrame() {
        if (autoRecycle) {
            if (pcm != NULL) {
                free(pcm);
                pcm = NULL;
            }
            if (ext_data != NULL) {
                free(ext_data);
                ext_data = NULL;
            }
        }
    }

};

#endif //EVOPLAYER_AUDIO_FRAME_H
