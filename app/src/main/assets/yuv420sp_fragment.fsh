#version 300 es

precision mediump float;

in vec2 texture_coord;
layout(location = 0) uniform sampler2D sampler_y;
layout(location = 1) uniform sampler2D sampler_uv;

out vec4 out_color;

void main() {
    vec3 yuv;
    yuv.x = texture(sampler_y, texture_coord).r - (16.0 / 255.0);
    yuv.y = texture(sampler_uv, texture_coord).r- 0.5;
    yuv.z = texture(sampler_uv, texture_coord).a- 0.5;

    vec3 rgb = mat3(1.164, 1.164, 1.164, 0.0, -0.213, 2.112, 1.793, -0.533, 0.0) * yuv;
    out_color = vec4(rgb, 1.0);
}