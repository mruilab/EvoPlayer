package com.mruilab.evoplayer.beauty;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.mruilab.evoplayer.R;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class FrontPageActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private boolean hasPermissions = false;
    private static final int RC_READ_EXTERNAL_STORAGE = 1001;

    private Button mChooseVideoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);
        checkPermissions();

        mChooseVideoBtn = findViewById(R.id.choose_video_btn);

        mChooseVideoBtn.setOnClickListener(view -> {
            if (!hasPermissions) {
                checkPermissions();
            } else {
                //跳转相册去选择文件
                Intent intent = new Intent(this, BeautyActivity.class);
                startActivity(intent);
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
}
