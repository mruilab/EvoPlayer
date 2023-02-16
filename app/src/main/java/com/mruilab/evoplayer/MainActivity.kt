package com.mruilab.evoplayer

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mruilab.evoplayer.video.VideoItem
import com.mruilab.evoplayer.video.VideoListAdapter
import com.mruilab.evoplayer.video.VideoLoadListener
import com.mruilab.evoplayer.video.VideoLoader
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private var hasStoragePermissions: Boolean = false

    private lateinit var mSelectedVideoItem: VideoItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        val videoLoader = VideoLoader(this@MainActivity)
        videoLoader.loadDeviceVideos(object : VideoLoadListener {
            override fun onVideoLoaded(videoItems: List<VideoItem>) {
                val videoList: RecyclerView = findViewById(R.id.video_list)
                videoList.layoutManager = LinearLayoutManager(this@MainActivity)
                /**
                 * 设置为不支持数据更改时的动画
                 *
                 * fix bug:RecyclerView为选中的Item添加背景色，并移除上次选中Item的背景色时，旧位置会发生闪烁。
                 * 这是因为RecyclerView默认设置了DefaultItemAnimator动画，所以在调用notifyItemChanged()方
                 * 法时，会产生动画，发生闪烁现象。
                 */
                (videoList.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
                val adapter = VideoListAdapter(this@MainActivity)
                videoList.adapter = adapter
                adapter.setData(videoItems)

                adapter.setOnItemClickListener(object : VideoListAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        mSelectedVideoItem = videoItems[position]
                    }
                })
            }

            override fun onFailed(e: Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun setListeners() {
        findViewById<Button>(R.id.hw_player).setOnClickListener {
            jumpToActivity(HWPlayerActivity::class.java)
        }
        findViewById<Button>(R.id.ff_player).setOnClickListener {
            jumpToActivity(FFmpegPlayerActivity::class.java)
        }
        findViewById<Button>(R.id.ff_gl_player).setOnClickListener {
            jumpToActivity(FFGLPlayerActivity::class.java)
        }
    }

    private fun jumpToActivity(activity: Class<*>) {
        val intent = Intent(this, activity)
        intent.putExtra("video_path", mSelectedVideoItem.path)
        startActivity(intent)
    }

    private fun checkPermissions() {
        val perms: Array<String> = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!EasyPermissions.hasPermissions(this, *perms)) {
            EasyPermissions.requestPermissions(
                this, "是否允许\"EvoPlayer\"访问您设备上的照片、媒体内容和文件？", 1001, *perms
            )
        } else {
            hasStoragePermissions = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == 1001) hasStoragePermissions = true
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }

}