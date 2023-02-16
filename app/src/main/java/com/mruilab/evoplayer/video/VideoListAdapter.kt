package com.mruilab.evoplayer.video

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mruilab.evoplayer.R

/**
 * RecyclerView支持单选功能。为选中的Item添加背景色，并移除上次选中Item的背景色。
 */
class VideoListAdapter(private val context: Context) :
    RecyclerView.Adapter<VideoListAdapter.ViewHolder>() {

    private var videoList: List<VideoItem> = ArrayList()
    private var onItemClickListener: OnItemClickListener? = null

    //选择的位置
    var selPosition = 0

    //临时记录上次选择的位置
    var tempPosition = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageCover: ImageView
        val imagePath: TextView

        init {
            imageCover = view.findViewById(R.id.video_cover)
            imagePath = view.findViewById(R.id.video_path)
        }
    }

    fun setData(data: List<VideoItem>) {
        videoList = data
//        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data: VideoItem = videoList[position]
        Glide.with(context).load(data.path).into(holder.imageCover)
        holder.imagePath.text = data.path

        holder.itemView.isSelected = holder.layoutPosition == selPosition

        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(holder.itemView, position)

            holder.itemView.isSelected = true
            //将旧的位置保存下来，用于后面把旧的位置颜色变回来
            tempPosition = selPosition
            //设置新的位置
            selPosition = holder.layoutPosition
            //更新旧位置
            notifyItemChanged(tempPosition)
        }
    }

    override fun getItemCount() = videoList.size

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}