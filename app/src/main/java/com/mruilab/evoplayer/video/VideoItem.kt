package com.mruilab.evoplayer.video

import android.os.Parcel
import android.os.Parcelable

class VideoItem : Parcelable {
    val path: String
    val duration: Int
    val width: Int
    val height: Int

    constructor(path: String, duration: Int, width: Int, height: Int) {
        this.path = path
        this.duration = duration
        this.width = width
        this.height = height
    }

    constructor(parcel: Parcel) {
        path = parcel.readString().toString()
        duration = parcel.readInt()
        width = parcel.readInt()
        height = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path)
        parcel.writeInt(duration)
        parcel.writeInt(width)
        parcel.writeInt(height)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoItem> {
        override fun createFromParcel(parcel: Parcel): VideoItem {
            return VideoItem(parcel)
        }

        override fun newArray(size: Int): Array<VideoItem?> {
            return arrayOfNulls(size)
        }
    }

}