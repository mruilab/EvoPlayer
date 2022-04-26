//
// Created by mruilab on 2022/4/3.
//

#include "player.h"
#include "native_render.h"

Player::Player(JNIEnv *env, jstring path, jobject surface) {
    m_video_decoder = new VideoDecoder();

    //本地窗口播放
    m_video_render = new NativeRender(env, surface);
    m_video_decoder->SetRender(m_video_render);
    m_video_decoder->CreateDecoder(env, path);
}

Player::~Player() {
    // 此处不需要 delete 成员指针
    // 在BaseDecoder中的线程已经使用智能指针，会自动释放
}

void Player::play() {
    if (m_video_decoder != NULL) {
        m_video_decoder->GoOn();
    }
}

void Player::pause() {
    if (m_video_decoder != NULL) {
        m_video_decoder->Pause();
    }
}