//
// Created by mruilab on 2022/4/15.
//

#ifndef EVOPLAYER_IMAGE_UTIL_H
#define EVOPLAYER_IMAGE_UTIL_H


extern "C" {
#include <libavutil/frame.h>
};

#define IMAGE_FORMAT_RGBA           0x01
#define IMAGE_FORMAT_NV21           0x02
#define IMAGE_FORMAT_NV12           0x03
#define IMAGE_FORMAT_I420           0x04

//视频数据目标格式
static const AVPixelFormat DST_FORMAT = AV_PIX_FMT_YUV420P;

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


#endif //EVOPLAYER_IMAGE_UTIL_H
