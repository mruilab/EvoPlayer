//
// 解码状态定义
// Created by mruilab on 2022/4/2.
//

#ifndef EVOPLAYER_DECODE_STATE_H
#define EVOPLAYER_DECODE_STATE_H

enum DecodeState {
    STOP,
    PREPARE,
    START,
    DECODING,
    PAUSE,
    FINISH
};

#endif //EVOPLAYER_DECODE_STATE_H
