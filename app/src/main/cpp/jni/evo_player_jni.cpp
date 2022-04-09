//
// Created by mruilab on 2022/3/31.
//

#include "evo_player_jni.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "native_window_player.h"
#include "player.h"
#include "gl_player.h"
#include "logger.h"

extern "C" {
#include <libavcodec/version.h>
#include <libavcodec/avcodec.h>
#include <libavcodec/jni.h>
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
        {"getCodecSupport",  "()V",                                         (void *) get_codec_support},
        {"playVideo",        "(Ljava/lang/String;Landroid/view/Surface;)I", (void *) play_video},
        {"createPlayer",     "(Ljava/lang/String;Landroid/view/Surface;)J", (void *) create_player},
        {"play",             "(J)V",                                        (void *) play},
        {"createGLPlayer",   "(Ljava/lang/String;Landroid/view/Surface;)J", (void *) create_gl_player},
        {"playOrPause",      "(J)V",                                        (void *) play_or_pause},
        {"stop",             "(J)V",                                        (void *) stop},
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
    // 将 JavaVM 设置给 FFmpeg
    av_jni_set_java_vm(vm, 0);
    return JNI_VERSION_1_6;
}

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

/**
 * 打印 AVCodec 支持的格式列表
 * @param env
 * @param obj
 */
void get_codec_support(JNIEnv *env, jobject obj) {
    char info[1024] = {0};
    AVCodec *c_temp = av_codec_next(NULL);
    while (c_temp != NULL) {
        if (c_temp->decode != NULL) {
            strcat(info, "[Dec]");
        } else {
            strcat(info, "[Enc]");
        }

        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                strcat(info, "[Video]");
                break;
            case AVMEDIA_TYPE_AUDIO:
                strcat(info, "[Audio]");
                break;
            default:
                strcat(info, "[Other]");
                break;
        }
        LOGI("Codec Support", "%s %10s\n", info, c_temp->name);
        c_temp = c_temp->next;
        memset(info, 0, 1024);
    }
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

long create_gl_player(JNIEnv *env, jobject obj, jstring video_path, jobject surface) {
    GLPlayer *player = new GLPlayer(env, video_path);
    player->SetSurface(surface);
    return (uintptr_t) player;
}

void play_or_pause(JNIEnv *env, jobject obj, jlong player) {
    GLPlayer *p = (GLPlayer *) player;
    p->PlayOrPause();
}

void stop(JNIEnv *env, jobject obj, jlong player) {
    GLPlayer *p = (GLPlayer *) player;
    p->Release();
}


