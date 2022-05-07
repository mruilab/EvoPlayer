package com.mruilab.evoplayer.utils

import java.nio.ByteBuffer

enum class ColorFormat {
    UNKNOWN,
    I420,
    YV12,
    NV12,
    NV21,
    NV21_32M
}

class ImageUtils {

    companion object {

        /**
         * 解决MediaCodec解码后数据对齐导致的问题
         */
        fun cropYUV(
            src: ByteBuffer,
            dst: ByteArray,
            srcWidth: Int,
            srcHeight: Int,
            dstWidth: Int,
            dstHeight: Int
        ) {
            if (srcWidth == dstWidth && srcHeight == dstHeight) {
                src.get(dst)
            } else {
                for (i in 0 until dstHeight) {
                    src.position(i * srcWidth)
                    src.get(dst, i * dstWidth, dstWidth)
                }

                for (i in 0 until dstHeight / 2) {
                    src.position(srcWidth * srcHeight + i * srcWidth)
                    src.get(dst, dstWidth * dstHeight + i * dstWidth, dstWidth)
                }
            }
        }
    }
}