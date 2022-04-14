//
// Created by liurui on 2022/4/14.
//

#include "shader.h"

static const char f_shader[] =
        SHADER_STRING(
                precision mediump float;
                uniform sampler2D u_texture;
                in vec2 v_texCoord;
                out vec4 out_color;
                void main() {
                    vec4 color = texture(u_texture, v_texCoord);
                    color.a = 1.0;
                    out_color = color;
//                    float gray = (color.r + color.g + color.b) / 3.0;
//                    out_color = vec4(gray, gray, gray, 1.0);
                }
        );

const char *rgba_fragment_shader() {
    return f_shader;
}
