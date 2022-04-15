//
// Created by mruilab on 2022/4/15.
//

#ifndef EVOPLAYER_IMAGE_UTIL_H
#define EVOPLAYER_IMAGE_UTIL_H


extern "C" {
#include <libavutil/frame.h>
};

static void obtainYUV420p(AVFrame *srcFrame, AVFrame *dstFrame) {
    int w = srcFrame->width;
    int h = srcFrame->height;

    int l1 = srcFrame->linesize[0];
    int l2 = srcFrame->linesize[1];
    int l3 = srcFrame->linesize[2];

    for (int i = 0; i < h; i++) {
        memcpy(dstFrame->data[0] + w * i, srcFrame->data[0] + l1 * i, sizeof(unsigned char) * w);
    }
    for (int i = 0; i < h / 2; i++) {
        memcpy(dstFrame->data[1] + w / 2 * i, srcFrame->data[1] + l2 * i,
               sizeof(unsigned char) * w / 2);
        memcpy(dstFrame->data[2] + w / 2 * i, srcFrame->data[2] + l3 * i,
               sizeof(unsigned char) * w / 2);
    }
}

static void obtainNV12(AVFrame *srcFrame, AVFrame *dstFrame) {
    int w = srcFrame->width;
    int h = srcFrame->height;

    int l1 = srcFrame->linesize[0];
    int l2 = srcFrame->linesize[1];
    for (int i = 0; i < h; i++) {
        memcpy(dstFrame->data[0] + w * i, srcFrame->data[0] + l1 * i, sizeof(unsigned char) * w);
    }
    for (int i = 0; i < h / 2; i++) {
        memcpy(dstFrame->data[1] + w * i, srcFrame->data[1] + l2 * i, sizeof(unsigned char) * w);
    }
}


#endif //EVOPLAYER_IMAGE_UTIL_H
