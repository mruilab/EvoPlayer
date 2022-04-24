//
// Created by mruilab on 2022/4/8.
//

#include "video_drawer.h"
#include "shader.h"

VideoDrawer::VideoDrawer() : Drawer(0, 0) {

}

VideoDrawer::~VideoDrawer() {

}

void VideoDrawer::InitRender(JNIEnv *env, int video_width, int video_height, int *dst_size) {
    SetVideoSize(video_width, video_height);
    dst_size[0] = video_width;
    dst_size[1] = video_height;
}

void VideoDrawer::Render(OneFrame *one_frame) {
    m_frame = one_frame->frame;
    switch (m_frame->format) {
        case AV_PIX_FMT_YUV420P:
            SetTextureNum(3);
            break;
        case AV_PIX_FMT_NV12:
        case AV_PIX_FMT_NV21:
            SetTextureNum(2);
            break;
        default:
            SetTextureNum(1);
            break;
    }
}

void VideoDrawer::ReleaseRender() {
    if (m_frame != NULL) {
        av_frame_free(&m_frame);
        m_frame = NULL;
    }
}

const char *VideoDrawer::GetVertexShader() {
    return default_vertex_shader();
}

const char *VideoDrawer::GetFragmentShader() {
    return default_fragment_shader();
}

void VideoDrawer::InitCstShaderHandler() {

}

void VideoDrawer::BindTexture() {
    if (m_frame == NULL) return;
    switch (m_frame->format) {
        case AV_PIX_FMT_RGBA:
            ActivateTexture();
            glUniform1i(3, IMAGE_FORMAT_RGBA);
            break;
        case AV_PIX_FMT_YUV420P:
            ActivateTexture(0, GL_TEXTURE_2D);
            ActivateTexture(1, GL_TEXTURE_2D);
            ActivateTexture(2, GL_TEXTURE_2D);
            glUniform1i(3, IMAGE_FORMAT_I420);
            break;
        case AV_PIX_FMT_NV21:
            ActivateTexture(0, GL_TEXTURE_2D);
            ActivateTexture(1, GL_TEXTURE_2D);
            glUniform1i(3, IMAGE_FORMAT_NV21);
            break;
        case AV_PIX_FMT_NV12:
            ActivateTexture(0, GL_TEXTURE_2D);
            ActivateTexture(1, GL_TEXTURE_2D);
            glUniform1i(3, IMAGE_FORMAT_NV12);
            break;
    }
}

void VideoDrawer::PrepareDraw() {
    if (m_frame == NULL) return;
    switch (m_frame->format) {
        case AV_PIX_FMT_YUV420P:
            glActiveTexture(GL_TEXTURE0);
            glTexImage2D(GL_TEXTURE_2D, 0,
                         GL_LUMINANCE,
                         origin_width(), origin_height(),
                         0,
                         GL_LUMINANCE,
                         GL_UNSIGNED_BYTE,
                         m_frame->data[0]);
            glActiveTexture(GL_TEXTURE1);
            glTexImage2D(GL_TEXTURE_2D, 0,
                         GL_LUMINANCE,
                         origin_width() >> 1, origin_height() >> 1,
                         0,
                         GL_LUMINANCE,
                         GL_UNSIGNED_BYTE,
                         m_frame->data[1]);
            glActiveTexture(GL_TEXTURE2);
            glTexImage2D(GL_TEXTURE_2D, 0,
                         GL_LUMINANCE,
                         origin_width() >> 1, origin_height() >> 1,
                         0,
                         GL_LUMINANCE,
                         GL_UNSIGNED_BYTE,
                         m_frame->data[2]);
            break;
        case AV_PIX_FMT_NV21:
        case AV_PIX_FMT_NV12:
            glActiveTexture(GL_TEXTURE0);
            glTexImage2D(GL_TEXTURE_2D, 0,
                         GL_LUMINANCE,
                         origin_width(), origin_height(),
                         0,
                         GL_LUMINANCE,
                         GL_UNSIGNED_BYTE,
                         m_frame->data[0]);
            glActiveTexture(GL_TEXTURE1);
            glTexImage2D(GL_TEXTURE_2D, 0,
                         GL_LUMINANCE_ALPHA,
                         origin_width() >> 1, origin_height() >> 1,
                         0,
                         GL_LUMINANCE_ALPHA,
                         GL_UNSIGNED_BYTE,
                         m_frame->data[1]);
            break;
        default:
            glTexImage2D(GL_TEXTURE_2D, 0, // level一般为0
                         GL_RGBA, //纹理内部格式
                         origin_width(), origin_height(), // 画面宽高
                         0, // 必须为0
                         GL_RGBA, // 数据格式，必须和上面的纹理格式保持一直
                         GL_UNSIGNED_BYTE, // RGBA每位数据的字节数，这里是BYTE: 1 byte
                         m_frame->data[0]);// 画面数据
            break;
    }
}

void VideoDrawer::DoneDraw() {
}