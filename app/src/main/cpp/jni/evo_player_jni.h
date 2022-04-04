//
// Created by mruilab on 2022/3/31.
//

#ifndef EVOPLAYER_EVO_PLAYER_JNI_H
#define EVOPLAYER_EVO_PLAYER_JNI_H

#include <jni.h>

jstring get_ffmpeg_version(JNIEnv *env, jobject obj);

jint play_video(JNIEnv *env, jobject obj,
                jstring video_path, jobject surface);

jint create_player(JNIEnv *env, jobject obj,
                   jstring video_path, jobject surface);

void play(JNIEnv *env, jobject obj, jint player);

#endif //EVOPLAYER_EVO_PLAYER_JNI_H
