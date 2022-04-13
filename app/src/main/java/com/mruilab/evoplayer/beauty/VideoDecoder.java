package com.mruilab.evoplayer.beauty;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoDecoder {

    private static final String TAG = VideoDecoder.class.getSimpleName();

    public final static int COLOR_FORMAT_I420 = 1;
    public final static int COLOR_FORMAT_NV21 = 2;
    public final static int COLOR_FORMAT_NV12 = 3;

    private static final int DECODER_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;
    private static final long DEFAULT_TIMEOUT_US = 10000;

    private int mOutputFormat = COLOR_FORMAT_NV12;
    private byte[] mYuvBuffer;
    private volatile boolean mStop = false;

    private Surface mSurface = null;
    private VideoExtractor mExtractor;

    public void setOutputFormat(int outputFormat) {
        mOutputFormat = outputFormat;
    }

    public int getOutputFormat() {
        return mOutputFormat;
    }

    public void stop() {
        mStop = true;
    }

    public void setSurfaceView(Surface surface) {
        mSurface = surface;
    }

    public void decode(String videoFilePath, DecodeCallback decodeCallback) {
        mStop = false;

        MediaCodec decoder = null;
        try {
            mExtractor = new VideoExtractor(videoFilePath);
            int trackIndex = mExtractor.getVideoTrackId();
            if (trackIndex < 0) {
                Log.e(TAG, "No video track found in " + videoFilePath);
                return;
            }
            MediaFormat mediaFormat = mExtractor.getVideoFormat();
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(mime);
            if (isColorFormatSupported(DECODER_COLOR_FORMAT, decoder.getCodecInfo().getCapabilitiesForType(mime))) {
                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, DECODER_COLOR_FORMAT);
                Log.i(TAG, "set decode color format to type " + DECODER_COLOR_FORMAT);
            } else {
                Log.i(TAG, "unable to set decode color format, color format type " + DECODER_COLOR_FORMAT +
                        "not supported");
            }
            int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
            int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
            Log.d(TAG, "decode video width: " + width + ", height: " + height);
            int yuvLength = width * height * 3 / 2;
            if (mYuvBuffer == null || mYuvBuffer.length != yuvLength) {
                mYuvBuffer = new byte[yuvLength];
            }
            decoder.configure(mediaFormat, mSurface, null, 0);
            decoder.start();
            decodeFormatToImage(decoder, mExtractor, width, height, decodeCallback);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (decoder != null) {
                decoder.stop();
                decoder.release();
                decoder = null;
            }
            if (mExtractor != null) {
                mExtractor.release();
                mExtractor = null;
            }
        }
    }

    private boolean isColorFormatSupported(int colorFormat, MediaCodecInfo.CodecCapabilities caps) {
        for (int c : caps.colorFormats) {
            if (c == colorFormat) return true;
        }
        return false;
    }

    private void decodeFormatToImage(MediaCodec decoder, VideoExtractor extractor, int width, int height,
                                     DecodeCallback decodeCallback) {
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        // EOS -> END OF STREAM
        boolean inputEOS = false;
        boolean outputEOS = false;
        int outputFrameCount = 0;
        long startMs = System.currentTimeMillis();

        int inputBufferId, outputBufferId;
        ByteBuffer inputBuffer;
        int sampleSize;
        Image image;

        long cvStartMs;

        while (!mStop && !outputEOS) {
            if (!inputEOS) {
                inputBufferId = decoder.dequeueInputBuffer(DEFAULT_TIMEOUT_US);
                if (inputBufferId >= 0) {
                    inputBuffer = decoder.getInputBuffer(inputBufferId);
                    sampleSize = extractor.readBuffer(inputBuffer);
                    if (sampleSize < 0) {
                        decoder.queueInputBuffer(inputBufferId, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputEOS = true;
                        Log.i(TAG, "inputEOS is true");
                    } else {
                        decoder.queueInputBuffer(inputBufferId, 0, sampleSize, extractor.getCurSampleTime(), 0);
                    }
                }
            }
            outputBufferId = decoder.dequeueOutputBuffer(info, DEFAULT_TIMEOUT_US);
            if (outputBufferId >= 0) {
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    outputEOS = true;
                    hasLogImageInfo = false;
                    Log.i(TAG, "outputEOS is true");
                }
                if (info.size > 0) {
                    outputFrameCount++;
                    //注意，某些机型当MediaCodeC配置了输出Surface时，getOutputBuffer/getOutputImage返回null
                    image = decoder.getOutputImage(outputBufferId);
                    if (mSurface == null && image != null) {
                        logImageInfo(image);
                        cvStartMs = System.currentTimeMillis();
                        mYuvBuffer = getDataFromImage(image, mOutputFormat);
                        Log.i(TAG, "current frame:" + outputFrameCount +
                                ",get yuv time:" + (System.currentTimeMillis() - cvStartMs));
                        image.close();
                        if (decodeCallback != null) {
                            decodeCallback.onDecode(mYuvBuffer, width, height,
                                    outputFrameCount, info.presentationTimeUs);
                        }
                    }
                    //帧率控制
                    while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    decoder.releaseOutputBuffer(outputBufferId, true);
                }
            }
        }
        if (decodeCallback != null) {
            if (mStop) {
                decodeCallback.onStop();
            } else {
                decodeCallback.onFinish();
            }
        }
    }

    /**
     * 该方法在低端机(红米3S)上耗时大概为60ms，所以不能用于渲染播放
     *
     * @param image
     * @param colorFormat
     * @return
     */
    private static byte[] getDataFromImage(Image image, int colorFormat) {
        if (colorFormat != COLOR_FORMAT_I420 && colorFormat != COLOR_FORMAT_NV21 &&
                colorFormat != COLOR_FORMAT_NV12) {
            throw new IllegalArgumentException("only support COLOR_FORMAT_I420 " +
                    "and COLOR_FORMAT_NV21 " + "and COLOR_FORMAT_NV12");
        }
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();


        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];

        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FORMAT_I420) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FORMAT_NV21) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    } else if (colorFormat == COLOR_FORMAT_NV12) {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FORMAT_I420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FORMAT_NV21) {
                        channelOffset = width * height;
                        outputStride = 2;
                    } else if (colorFormat == COLOR_FORMAT_NV12) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();

            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }

    private boolean hasLogImageInfo = false;

    private void logImageInfo(Image image) {
        if (hasLogImageInfo) return;
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        Log.i(TAG, "Image info\n" +
                "width:" + width + "\nheight:" + height + "\nformat:" + format +
                "\nY\n" +
                "len:" + planes[0].getBuffer().remaining() +
                "\nrowStride:" + planes[0].getRowStride() +
                "\npixelStride:" + planes[0].getPixelStride() +
                "\nU\n" +
                "len:" + planes[1].getBuffer().remaining() +
                "\nrowStride:" + planes[1].getRowStride() +
                "\npixelStride:" + planes[1].getPixelStride() +
                "\nV\n" +
                "len:" + planes[2].getBuffer().remaining() +
                "\nrowStride:" + planes[2].getRowStride() +
                "\npixelStride:" + planes[2].getPixelStride());
        hasLogImageInfo = true;
    }

    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }

    public void release() {
        if (mExtractor != null) {
            mExtractor.release();
            mExtractor = null;
        }
    }

    public interface DecodeCallback {
        // 返回的yuv数据格式由OUTPUT_COLOR_FORMAT指定
        void onDecode(byte[] yuv, int width, int height, int frameCount, long presentationTimeUs);

        //解码完成
        void onFinish();

        //异常中断
        void onStop();
    }

}
