//
// Created by mruilab on 2022/4/8.
//

#include "video_drawer.h"

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
    cst_data = one_frame->data;
}

void VideoDrawer::ReleaseRender() {

}

static GLbyte vShaderStr[] =
        "attribute vec4 aPosition;\n"
        "uniform mat4 uMatrix;\n"
        "attribute vec2 aCoordinate;\n"
        "varying vec2 vCoordinate;\n"
        "void main() {\n"
        "  gl_Position = uMatrix*aPosition;\n"
        "  vCoordinate = aCoordinate;\n"
        "}";

const char *VideoDrawer::GetVertexShader() {
    return (char *) vShaderStr;
}

static GLbyte fShaderStr[] =
        "precision mediump float;\n"
        "uniform sampler2D uTexture;\n"
        "varying vec2 vCoordinate;\n"
        "void main() {\n"
        "  vec4 color = texture2D(uTexture, vCoordinate);\n"
        "  color.a = 1.0;\n"
        "  gl_FragColor = color;\n"
        //        "  float gray = (color.r + color.g + color.b)/3.0;\n"
        //        "  gl_FragColor = vec4(gray, gray, gray, 1.0);\n"
        "}";

const char *VideoDrawer::GetFragmentShader() {
    return (char *) fShaderStr;
}

void VideoDrawer::InitCstShaderHandler() {

}

void VideoDrawer::BindTexture() {
    ActivateTexture();
}

void VideoDrawer::PrepareDraw() {
    if (cst_data != NULL) {
        glTexImage2D(GL_TEXTURE_2D, 0, // level一般为0
                     GL_RGBA, //纹理内部格式
                     origin_width(), origin_height(), // 画面宽高
                     0, // 必须为0
                     GL_RGBA, // 数据格式，必须和上面的纹理格式保持一直
                     GL_UNSIGNED_BYTE, // RGBA每位数据的字节数，这里是BYTE​: 1 byte
                     cst_data);// 画面数据
    }
}

void VideoDrawer::DoneDraw() {

}