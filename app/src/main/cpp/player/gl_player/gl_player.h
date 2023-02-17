//
// Created by mruilab on 2022/4/8.
//

#ifndef EVOPLAYER_GL_PLAYER_H
#define EVOPLAYER_GL_PLAYER_H

#include "video_decoder.h"
#include "drawer_proxy.h"
#include "opengl_render.h"
#include "video_drawer.h"
#include "audio_decoder.h"

class GLPlayer {
private:
    VideoDecoder *m_video_decoder;
    OpenGLRender *m_gl_render;

    DrawerProxy *m_video_drawer_proxy;
    VideoDrawer *m_video_drawer;

    AudioDecoder *m_audio_decoder;
    AudioRender *m_audio_render;

    int m_video_create_ret = -1;
    int m_audio_create_ret = -1;

public:
    GLPlayer(JNIEnv *env, jstring path);

    ~GLPlayer();

    void SetSurface(jobject surface);

    void SetSurfaceSize(int width, int height);

    void PlayOrPause();

    void Release();

};


#endif //EVOPLAYER_GL_PLAYER_H
