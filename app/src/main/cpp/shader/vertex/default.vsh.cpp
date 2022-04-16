//
// Created by mruilab on 2022/4/14.
//

#include "shader.h"

static const char v_shader[] =
        SHADER_STRING(
                layout(location = 0) in vec4 a_position;
                layout(location = 1) in vec2 a_texCoord;
                uniform mat4 u_MVPMatrix;
                out vec2 v_texCoord;
                void main() {
                    gl_Position = u_MVPMatrix * a_position;
                    v_texCoord = a_texCoord;
                }
        );

const char *default_vertex_shader() {
    return v_shader;
}
