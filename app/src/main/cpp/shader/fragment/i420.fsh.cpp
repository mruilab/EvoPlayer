//
// Created by mruilab on 2022/4/15.
//

#include "shader.h"

static const char f_shader[] =
        SHADER_STRING(
                precision mediump float;
                in vec2 v_texCoord;
                out vec4 out_color;
                layout(location = 0) uniform sampler2D y_texture;
                layout(location = 1) uniform sampler2D u_texture;
                layout(location = 2) uniform sampler2D v_texture;

                void main() {
                    vec3 yuv;
                    yuv.x = texture(y_texture, v_texCoord).r;
                    yuv.y = texture(u_texture, v_texCoord).r - 0.5;
                    yuv.z = texture(v_texture, v_texCoord).r - 0.5;

                    vec3 rgb = mat3(1.0, 1.0, 1.0,
                                    0.0, -0.39465, 2.03211,
                                    1.13983, -0.58060, 0.0) * yuv;
                    out_color = vec4(rgb, 1.0);
                }
        );

const char *i420_fragment_shader() {
    return f_shader;
}

