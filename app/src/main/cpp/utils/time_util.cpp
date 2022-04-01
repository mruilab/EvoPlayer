//
// Created by mruilab on 2022/4/1.
//

#include "time_util.h"
#include <sys/time.h>

long TimeUtil::currentTimeMillis() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}
