package com.mruilab.evoplayer.beauty;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.mruilab.evoplayer.R;
import com.mruilab.evoplayer.utils.Uri2PathUtil;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class FrontPageActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = FrontPageActivity.class.getSimpleName();

    private boolean hasPermissions = false;
    private static final int RC_READ_EXTERNAL_STORAGE = 1001;

    ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null)
                            return;
                        Uri uri = result.getData().getData();
                        if (uri == null) return;
                        String path = Uri2PathUtil.getRealPathFromUri(FrontPageActivity.this, uri);
//                        if (!checkIsVideo(FrontPageActivity.this, path)) {
//                            Toast.makeText(FrontPageActivity.this, "请选择正确的视频文件", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
                        Intent intent = new Intent(FrontPageActivity.this, BeautyActivity.class);
                        intent.putExtra("path", path);
                        startActivity(intent);
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);
        checkPermissions();

        findViewById(R.id.lyt_select_data_video).setOnClickListener(view -> {
            if (!hasPermissions) {
                checkPermissions();
            } else {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("video/*");
                launcher.launch(intent);
            }
        });
    }

    private void checkPermissions() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(
                    this,
                    "是否允许\"EvoPlayer\"访问您设备上的照片、媒体内容和文件？",
                    RC_READ_EXTERNAL_STORAGE,
                    perms
            );
        } else {
            hasPermissions = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode == RC_READ_EXTERNAL_STORAGE) {
            hasPermissions = true;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    /**
     * 校验文件是否是视频
     *
     * @param path String
     * @return Boolean
     */
    public static Boolean checkIsVideo(Context context, String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, Uri.fromFile(new File(path)));
            String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
            return "yes".equals(hasVideo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
