//
// Created by mruilab on 2022/4/8.
//

#ifndef EVOPLAYER_OPENGL_PIXEL_RECEIVER_H
#define EVOPLAYER_OPENGL_PIXEL_RECEIVER_H

#include <stdint.h>

class OpenGLPixelReceiver {

public:
    virtual void ReceivePixel(uint8_t *rgba) = 0;
};

#endif //EVOPLAYER_OPENGL_PIXEL_RECEIVER_H
