package com.mruilab.evoplayer.decoder

import com.mruilab.evoplayer.utils.ColorFormat
import com.mruilab.evoplayer.utils.ImageUtils

interface DecodeCallback {
    // 返回的yuv数据格式由OUTPUT_COLOR_FORMAT指定
    fun onDecode(
        yuv: ByteArray,
        width: Int,
        height: Int,
        colorFormat: ColorFormat,
        frameId: Int,
        presentationTimeUs: Long
    )

    // 解码完成
    fun onDecodeFinish()

    //异常中断
    fun onDecodeStop()
}