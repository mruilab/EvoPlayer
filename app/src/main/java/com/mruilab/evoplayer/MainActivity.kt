package com.mruilab.evoplayer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.mruilab.evoplayer.utils.Uri2PathUtils
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener,
    EasyPermissions.PermissionCallbacks {

    private var hasStoragePermissions: Boolean = false

    lateinit var mJumpToActivity: Class<*>

    private val mLauncher =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode != Activity.RESULT_OK || result.data == null) {
                return@registerForActivityResult
            }
            val uri = result.data!!.data ?: return@registerForActivityResult
            val videoPath = Uri2PathUtils.getRealPathFromUri(this, uri);
            if (!checkIsVideo(this, videoPath)) {
                Toast.makeText(this, "请选择正确的视频文件", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            val intent = Intent(this, mJumpToActivity).apply {
                putExtra("video_path", videoPath)
            }
            startActivity(intent)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
        setClickListeners()
    }

    private fun setClickListeners() {
        findViewById<Button>(R.id.ff_gl_player).setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ff_gl_player -> {
                openDocument(FFGLPlayerActivity::class.java)
            }
        }
    }

    private fun openDocument(activity: Class<*>) {
        if (hasStoragePermissions) {
            mJumpToActivity = activity
            val docIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "video/*"
            }
            mLauncher.launch(docIntent)
        } else {
            checkPermissions()
        }
    }

    private fun checkPermissions() {
        val perms: Array<String> = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!EasyPermissions.hasPermissions(this, *perms)) {
            EasyPermissions.requestPermissions(
                this,
                "是否允许\"EvoPlayer\"访问您设备上的照片、媒体内容和文件？",
                1001,
                *perms
            )
        } else {
            hasStoragePermissions = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == 1001)
            hasStoragePermissions = true
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }

    /**
     * 校验文件是否是视频
     *
     * @param path String
     * @return Boolean
     */
    private fun checkIsVideo(context: Context, path: String): Boolean {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, Uri.fromFile(File(path)))
            val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
            return "yes" == hasVideo
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

}