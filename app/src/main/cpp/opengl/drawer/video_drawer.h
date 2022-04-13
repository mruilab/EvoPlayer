//
// Created by mruilab on 2022/4/8.
//

#ifndef EVOPLAYER_VIDEO_DRAWER_H
#define EVOPLAYER_VIDEO_DRAWER_H

#include "drawer.h"
#include "egl_surface.h"
#include "video_render.h"

class VideoDrawer : public Drawer, public VideoRender {

private:
    AVFrame *m_frame = NULL;

public:

    VideoDrawer();

    ~VideoDrawer();

    void InitRender(JNIEnv *env, int video_width, int video_height, int *dst_size) override;

    void Render(OneFrame *one_frame) override;

    void ReleaseRender() override;

    const char *GetVertexShader() override;

    const char *GetFragmentShader() override;

    void InitCstShaderHandler() override;

    void BindTexture() override;

    void PrepareDraw() override;

    void DoneDraw() override;
};


#endif //EVOPLAYER_VIDEO_DRAWER_H
