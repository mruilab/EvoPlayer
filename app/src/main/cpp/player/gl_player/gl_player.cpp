//
// Created by mruilab on 2022/4/8.
//

#include "gl_player.h"
#include "native_render.h"
#include "opengl_render.h"
#include "def_drawer_proxy_impl.h"

GLPlayer::GLPlayer(JNIEnv *env, jstring path) {
    m_video_decoder = new VideoDecoder(env, path);

    // OpenGL 渲染
    m_video_drawer = new VideoDrawer();
    m_video_decoder->SetRender(m_video_drawer);

    DefDrawerProxyImpl *proxyImpl = new DefDrawerProxyImpl();
    proxyImpl->AddDrawer(m_video_drawer);

    m_video_drawer_proxy = proxyImpl;

    m_gl_render = new OpenGLRender(env, m_video_drawer_proxy);
}

GLPlayer::~GLPlayer() {
    // 此处不需要 delete 成员指针
    // 在BaseDecoder 和 OpenGLRender 中的线程已经使用智能指针，会自动释放相关指针
}

void GLPlayer::SetSurface(jobject surface) {
    m_gl_render->SetSurface(surface);
}

void GLPlayer::PlayOrPause() {
    if (!m_video_decoder->IsRunning()) {
        LOGI("Player", "播放视频")
        m_video_decoder->GoOn();
    } else {
        LOGI("Player", "暂停视频")
        m_video_decoder->Pause();
    }
}

void GLPlayer::Release() {
    m_gl_render->Stop();
    m_video_decoder->Stop();
}