//
// Created by mruilab on 2022/4/14.
//

#include "shader.h"

static const char f_shader[] =
        SHADER_STRING(
                precision mediump float;
                in vec2 v_texCoord;
                out vec4 out_color;
                layout(location = 0) uniform sampler2D s_texture0;

                void main() {
                    vec4 color = texture(s_texture0, v_texCoord);
                    color.a = 1.0;
                    out_color = color;
//                    float gray = (color.r + color.g + color.b) / 3.0;
//                    out_color = vec4(gray, gray, gray, 1.0);
                }
        );

const char *rgba_fragment_shader() {
    return f_shader;
}
