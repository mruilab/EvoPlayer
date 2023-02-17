//
// Created by mruilab on 2022/4/14.
//

#ifndef EVOPLAYER_SHADER_H
#define EVOPLAYER_SHADER_H

#if defined (__OPENGLES3__)
#define SHADER_STRING(x)   "#version 300 es\n"#x
#else
#define SHADER_STRING(x)   #x
#endif

#define IMAGE_FORMAT_RGBA           0x01
#define IMAGE_FORMAT_I420           0x02
#define IMAGE_FORMAT_NV21           0x03
#define IMAGE_FORMAT_NV12           0x04

const char *default_vertex_shader();

const char *default_fragment_shader();

const char *rgba_fragment_shader();

const char *i420_fragment_shader();

const char *nv21_fragment_shader();

const char *nv12_fragment_shader();


#endif //EVOPLAYER_SHADER_H
