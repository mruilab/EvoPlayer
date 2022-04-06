//
// Created by mruilab on 2022/4/3.
//

#ifndef EVOPLAYER_PLAYER_H
#define EVOPLAYER_PLAYER_H

#include "video_decoder.h"

class Player {
private:
    VideoDecoder *m_video_decoder;
    VideoRender *m_video_render;

public:
    Player(JNIEnv *env, jstring path, jobject surface);

    ~Player();

    void play();

    void pause();
};


#endif //EVOPLAYER_PLAYER_H
