//
// Created by mruilab on 2022/4/8.
//

#ifndef EVOPLAYER_EGL_SURFACE_H
#define EVOPLAYER_EGL_SURFACE_H

#include <android/native_window.h>
#include "egl_core.h"

class EglSurface {
private:
    const char *TAG = "EglSurface";

    ANativeWindow *m_native_window = NULL;

    EglCore *m_core;

    EGLSurface m_surface;

public:
    EglSurface();

    ~EglSurface();

    bool Init();

    void CreateEglSurface(ANativeWindow *native_window, int width, int height);

    void MakeCurrent();

    void SwapBuffers();

    void DestroyEglSurface();

    void Release();
};


#endif //EVOPLAYER_EGL_SURFACE_H
