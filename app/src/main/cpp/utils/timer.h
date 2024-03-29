//
// Created by mruilab on 2022/4/4.
//

#ifndef EVOPLAYER_TIMER_H
#define EVOPLAYER_TIMER_H

#include "sys/time.h"

static int64_t GetCurMsTime() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    int64_t ts = (int64_t)tv.tv_sec * 1000 + tv.tv_usec / 1000;
    return ts;
}

#endif //EVOPLAYER_TIMER_H
