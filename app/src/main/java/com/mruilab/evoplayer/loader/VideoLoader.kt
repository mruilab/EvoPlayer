package com.mruilab.evoplayer.loader

import android.content.Context
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoLoader @JvmOverloads constructor(val context: Context) {
    private val TAG = "VideoLoader"

    private var executorService: ExecutorService? = null

    fun loadDeviceVideos(listener: VideoLoadListener) {
        getExecutorService().execute(VideoLoadRunnable(listener, context))
    }

    fun abortLoadVideos() {
        if (executorService != null) {
            executorService?.shutdown()
            executorService = null
        }
    }

    private fun getExecutorService(): ExecutorService {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor()
        }
        return executorService!!
    }

    inner class VideoLoadRunnable @JvmOverloads constructor(
        private val listener: VideoLoadListener, val context: Context
    ) : Runnable {

        private val handler: Handler = Handler(Looper.getMainLooper())

        private val projection = arrayOf(
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT
        )

        override fun run() {
            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Video.Media.DATE_MODIFIED
            )

            if (cursor == null) {
                handler.post { listener.onFailed(NullPointerException()) }
                return
            }

            val temp: ArrayList<VideoItem> = ArrayList(cursor.count)
            if (cursor.moveToLast()) {
                do {
                    val path =
                        cursor.getString(cursor.getColumnIndexOrThrow(projection[0])) ?: continue
                    Log.d(TAG, "pick video from device path = $path")

                    var duration = cursor.getString(cursor.getColumnIndexOrThrow(projection[1]))
                    if (duration == null) duration = "0"
                    Log.d(TAG, "pick video from device duration = $duration")

                    var width = cursor.getString(cursor.getColumnIndexOrThrow(projection[2]))
                    if (width == null) width = "0"
                    Log.d(TAG, "pick video from device width = $width")

                    var height = cursor.getString(cursor.getColumnIndexOrThrow(projection[3]))
                    if (height == null) height = "0"
                    Log.d(TAG, "pick video from device height = $height")

                    val file = File(path)
                    if (file.exists()) {
                        temp.add(
                            VideoItem(
                                path,
                                Integer.valueOf(duration),
                                Integer.valueOf(width),
                                Integer.valueOf(height)
                            )
                        )
                    }
                } while (cursor.moveToPrevious())
            }
            cursor.close()
            handler.post { listener.onVideoLoaded(temp) }
        }

    }

}