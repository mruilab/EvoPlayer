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
    return rgba_fragment_shader();
}

void VideoDrawer::InitCstShaderHandler() {

}

void VideoDrawer::BindTexture() {
    ActivateTexture();
}

void VideoDrawer::PrepareDraw() {
    if (m_frame != NULL) {
        if (m_frame->format == AV_PIX_FMT_RGBA) {
            glTexImage2D(GL_TEXTURE_2D, 0, // level一般为0
                         GL_RGBA, //纹理内部格式
                         origin_width(), origin_height(), // 画面宽高
                         0, // 必须为0
                         GL_RGBA, // 数据格式，必须和上面的纹理格式保持一直
                         GL_UNSIGNED_BYTE, // RGBA每位数据的字节数，这里是BYTE: 1 byte
                         m_frame->data[0]);// 画面数据
        }
    }
}

void VideoDrawer::DoneDraw() {

}