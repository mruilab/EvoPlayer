package com.mruilab.evoplayer;

import android.app.Application;
import android.util.Log;

import com.faceunity.core.callback.OperateCallback;
import com.faceunity.core.faceunity.FURenderManager;
import com.faceunity.core.utils.FULogger;
import com.mruilab.evoplayer.utils.authpack;

public class MyApplication extends Application {
    private static String TAG = "EvoApplication";
    public static Application mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        registerFURender();
    }

    private void registerFURender(){
        FURenderManager.setKitDebug(FULogger.LogLevel.DEBUG);
        FURenderManager.setCoreDebug(FULogger.LogLevel.DEBUG);
        FURenderManager.registerFURender(this, authpack.A(), new OperateCallback() {
            @Override
            public void onSuccess(int code, String msg) {
                Log.d(TAG, "registerFURender success:" + msg);
            }

            @Override
            public void onFail(int code, String msg) {
                Log.e(TAG, "registerFURender errCode:" + code + ",errMsg:" + msg);
            }
        });
    }
}
