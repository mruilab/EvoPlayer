//
// Created by mruilab on 2022/4/8.
//

#include "gl_player.h"
#include "opengl_render.h"
#include "def_drawer_proxy_impl.h"
#include "opensl_render.h"

GLPlayer::GLPlayer(JNIEnv *env, jstring path) {
    m_video_decoder = new VideoDecoder();

    // OpenGL 渲染
    m_video_drawer = new VideoDrawer();
    m_video_decoder->SetRender(m_video_drawer);
    m_video_create_ret = m_video_decoder->CreateDecoder(env, path);

    DefDrawerProxyImpl *proxyImpl = new DefDrawerProxyImpl();
    proxyImpl->AddDrawer(m_video_drawer);

    m_video_drawer_proxy = proxyImpl;

    m_gl_render = new OpenGLRender(env, m_video_drawer_proxy);

    // 音频解码
    m_audio_decoder = new AudioDecoder();
    m_audio_create_ret = m_audio_decoder->CreateDecoder(env, path);
    m_audio_render = new OpenSLRender();
    m_audio_decoder->SetRender(m_audio_render);
}

GLPlayer::~GLPlayer() {
    // 此处不需要 delete 成员指针
    // 在BaseDecoder 和 OpenGLRender 中的线程已经使用智能指针，会自动释放相关指针
}

void GLPlayer::SetSurface(jobject surface) {
    m_gl_render->SetSurface(surface);
}

void GLPlayer::SetSurfaceSize(int width, int height) {
    m_gl_render->SetSurfaceSize(width, height);
}

void GLPlayer::PlayOrPause() {
    if (!m_video_decoder->IsRunning()) {
        LOGI("Player", "播放视频")
        m_video_decoder->GoOn();
    } else {
        LOGI("Player", "暂停视频")
        m_video_decoder->Pause();
    }
    if (!m_audio_decoder->IsRunning()) {
        LOGI("Player", "播放音频")
        m_audio_decoder->GoOn();
    } else {
        LOGI("Player", "暂停音频")
        m_audio_decoder->Pause();
    }
}

void GLPlayer::Release() {
    m_gl_render->Stop();
    if (m_video_create_ret == 0)
        m_video_decoder->Stop();
    if (m_audio_create_ret == 0)
        m_audio_decoder->Stop();
}