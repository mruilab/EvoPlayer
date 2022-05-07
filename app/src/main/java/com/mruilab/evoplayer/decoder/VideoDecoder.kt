package com.mruilab.evoplayer.decoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import com.mruilab.evoplayer.utils.ColorFormat
import com.mruilab.evoplayer.utils.ImageUtils
import java.nio.ByteBuffer

class VideoDecoder {
    private val TAG = VideoDecoder::class.java.simpleName

    companion object {
        const val DECODER_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        const val DEFAULT_TIMEOUT_US: Long = 10000
    }

    private var mYuv: ByteArray? = null
    var mStop: Boolean = false

    fun decode(videoPath: String, decodeCallback: DecodeCallback) {
        mStop = false
        val extractor = VideoExtractor(videoPath)
        val trackIndex = extractor.videoTrackId
        if (trackIndex < 0) {
            Log.e(TAG, "No video track found in $videoPath")
            return
        }
        val mediaFormat = extractor.videoFormat
        val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
        var decoder = MediaCodec.createDecoderByType(mime!!)
        if (isColorFormatSupported(
                DECODER_COLOR_FORMAT,
                decoder.codecInfo.getCapabilitiesForType(mime)
            )
        ) {
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, DECODER_COLOR_FORMAT)
            Log.i(TAG, "set decode color format to type $DECODER_COLOR_FORMAT")
        } else {
            Log.i(
                TAG,
                "unable to set decode color format, color format type $DECODER_COLOR_FORMAT not supported"
            )
        }

        val width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH)
        val height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)
        Log.d(TAG, "decode video width: $width, height: $height")
        val yuvLen = width * height * 3 / 2
        if (mYuv == null || mYuv!!.size != yuvLen) {
            mYuv = ByteArray(yuvLen)
        }
        decoder.configure(mediaFormat, null, null, 0)
        decoder.start()
        decodeFormatToImage(decoder, extractor, width, height, decodeCallback)
    }

    private fun decodeFormatToImage(
        decoder: MediaCodec, extractor: VideoExtractor, width: Int,
        height: Int, decodeCallback: DecodeCallback?
    ) {
        val info = MediaCodec.BufferInfo()
        // EOS -> END OF STREAM
        var inputEOS = false
        var outputEOS = false
        var outputFrameCount = 0

        var inputBuffer: ByteBuffer?
        var inputBufferId: Int

        var outputBuffer: ByteBuffer?
        var outputBufferId: Int

        var sampleSize: Int
        var startMs = System.currentTimeMillis()

        while (!mStop && !outputEOS) {
            if (!inputEOS) {
                inputBufferId = decoder.dequeueInputBuffer(DEFAULT_TIMEOUT_US)
                if (inputBufferId >= 0) {
                    inputBuffer = decoder.getInputBuffer(inputBufferId)
                    sampleSize = extractor.readBuffer(inputBuffer!!)
                    if (sampleSize < 0) {
                        decoder.queueInputBuffer(
                            inputBufferId, 0, 0, 0L,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        inputEOS = true
                        Log.i(TAG, "inputEOS is true")
                    } else {
                        decoder.queueInputBuffer(
                            inputBufferId, 0, sampleSize,
                            extractor.curSampleTime, 0
                        )
                    }
                }
            }

            outputBufferId = decoder.dequeueOutputBuffer(info, DEFAULT_TIMEOUT_US)
            if (outputBufferId >= 0) {
                if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    outputEOS = true
                    Log.i(TAG, "outputEOS is true")
                }
                if (info.size > 0) {
                    outputFrameCount++
                    //注意，某些机型当MediaCodeC配置了输出Surface时，getOutputBuffer/getOutputImage返回null
                    outputBuffer = decoder.getOutputBuffer(outputBufferId)
                    if (outputBuffer != null) {
                        val format: MediaFormat = decoder.outputFormat
                        Log.i(TAG, "output buffer:$format,len:" + outputBuffer.remaining())
                        val stride = format.getInteger(MediaFormat.KEY_STRIDE)
                        val sliceHeight = format.getInteger(MediaFormat.KEY_SLICE_HEIGHT)
                        val colorFormat =
                            getColorFormat(format.getInteger(MediaFormat.KEY_COLOR_FORMAT))
                        ImageUtils.cropYUV(outputBuffer, mYuv!!, stride, sliceHeight, width, height)
                        decodeCallback?.onDecode(
                            mYuv!!, width, height, colorFormat, outputFrameCount,
                            info.presentationTimeUs
                        )
                    }
                    //帧率控制
                    while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                        Thread.sleep(10)
                    }
                    decoder.releaseOutputBuffer(outputBufferId, true)
                }
            }
        }
        if (mStop) {
            decodeCallback?.onDecodeStop()
        } else {
            decodeCallback?.onDecodeFinish()
        }
    }

    fun isColorFormatSupported(colorFormat: Int, caps: MediaCodecInfo.CodecCapabilities): Boolean {
        for (format in caps.colorFormats) {
            if (format == colorFormat) return true
        }
        return false
    }

    fun getColorFormat(format: Int): ColorFormat {
        return when (format) {
            19 -> ColorFormat.I420
            20 -> ColorFormat.YV12
            21 -> ColorFormat.NV12
            39 -> ColorFormat.NV21
            2141391876 -> ColorFormat.NV21_32M
            else -> ColorFormat.UNKNOWN
        }
    }

}



