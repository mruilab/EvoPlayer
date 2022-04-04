//
// Created by mruilab on 2022/3/31.
//
#include <string>
#include "evo_player_jni.h"
#include "native_window_player.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "player.h"

extern "C" {
#include <libavcodec/version.h>
#include <libavcodec/avcodec.h>
#include <libavformat/version.h>
#include <libavutil/version.h>
#include <libavfilter/version.h>
#include <libswresample/version.h>
#include <libswscale/version.h>
}

/**
 * 动态注册
 */
JNINativeMethod methods[] = {
        {"getFFmpegVersion", "()Ljava/lang/String;",                        (void *) get_ffmpeg_version},
        {"playVideo",        "(Ljava/lang/String;Landroid/view/Surface;)I", (void *) play_video},
        {"createPlayer",     "(Ljava/lang/String;Landroid/view/Surface;)J", (void *) create_player},
        {"play",             "(J)V",                                        (void *) play},
};

jint registerNativeMethod(JNIEnv *env) {
    jclass cls = env->FindClass("com/mruilab/evoplayer/EvoPlayer");
    if (env->RegisterNatives(cls, methods, sizeof(methods) / sizeof(methods[0])) < 0) {
        return -1;
    }
    return 0;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    if (registerNativeMethod(env) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_6;
}

/**
 * getFFmpegVersion是静态方法，这样需要使用jclass
 * @param env
 * @param cls
 * @return
 */
jstring get_ffmpeg_version(JNIEnv *env, jobject obj) {
    char strBuffer[1024 * 4] = {0};
    strcat(strBuffer, "libavcodec : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVCODEC_VERSION));
    strcat(strBuffer, "\nlibavformat : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVFORMAT_VERSION));
    strcat(strBuffer, "\nlibavutil : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVUTIL_VERSION));
    strcat(strBuffer, "\nlibavfilter : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVFILTER_VERSION));
    strcat(strBuffer, "\nlibswresample : ");
    strcat(strBuffer, AV_STRINGIFY(LIBSWRESAMPLE_VERSION));
    strcat(strBuffer, "\nlibswscale : ");
    strcat(strBuffer, AV_STRINGIFY(LIBSWSCALE_VERSION));
    strcat(strBuffer, "\navcodec_configure : \n");
    strcat(strBuffer, avcodec_configuration());
    strcat(strBuffer, "\navcodec_license : ");
    strcat(strBuffer, avcodec_license());
    return env->NewStringUTF(strBuffer);
}

int play_video(JNIEnv *env, jobject obj, jstring videoPath, jobject surface) {
    const char *path = env->GetStringUTFChars(videoPath, NULL);

    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);
    if (0 == nativeWindow) {
        return -1;
    }
    NativeWindowPlayer player;
    player.playVideo(path, nativeWindow);
    env->ReleaseStringUTFChars(videoPath, path);
    return 0;
}

long create_player(JNIEnv *env, jobject obj, jstring video_path, jobject surface) {
    Player *player = new Player(env, video_path, surface);
    return (uintptr_t) player;
}

void play(JNIEnv *env, jobject obj, jlong player) {
    Player *p = (Player *) player;
    p->play();
}
