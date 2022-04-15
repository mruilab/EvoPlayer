//
// Created by mruilab on 2022/4/8.
//

#ifndef EVOPLAYER_DRAWER_H
#define EVOPLAYER_DRAWER_H

#include <glm.hpp>
#include <gtc/type_ptr.hpp>

extern "C" {
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>
};

#define TEXTURE_NUM 3

class Drawer {
private:
    const char *TAG = "Drawer";
    
    const GLfloat m_vertex_coors[8] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
    };

    const GLfloat m_texture_coors[8] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    };

    /**上下颠倒的顶点矩阵*/
    const GLfloat m_reserve_vertex_coors[8] = {
            -1.0f, 1.0f,
            1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f
    };

    glm::mat4 m_transform = glm::mat4(1.0f);

    float *m_matrix = NULL;

    GLuint m_program_id = 0;

    GLuint *m_texture_ids = new GLuint[TEXTURE_NUM];

    GLint m_vertex_matrix_handler = -1;

    GLint m_vertex_pos_handler = -1;

    GLint m_texture_pos_handler = -1;

    int m_origin_width = 0;

    int m_origin_height = 0;

    int m_display_width = 0;

    int m_display_height = 0;

    void CreateTextureId();

    void UpdateMVPMatrix();

    void CreateProgram();

    GLuint LoadShader(GLenum type, const GLchar *shader_code);

    void DoDraw();

public:
    Drawer(int origin_width, int origin_height);

    virtual ~Drawer();

    void Draw();

    void SetDisplaySize(int width, int height);

    int origin_width() {
        return m_origin_width;
    }

    int origin_height() {
        return m_origin_height;
    }

    bool IsReadyToDraw();

    /**
     * 释放OpenGL
     */
    void Release();

protected:

    void SetVideoSize(int width, int height);

    void ActivateTexture(GLenum index = 0, GLenum type = GL_TEXTURE_2D);

    virtual const char *GetVertexShader() = 0;

    virtual const char *GetFragmentShader() = 0;

    virtual void InitCstShaderHandler() = 0;

    virtual bool receiveFirstFrame() = 0;

    virtual void BindTexture() = 0;

    virtual void PrepareDraw() = 0;

    virtual void DoneDraw() = 0;
};


#endif //EVOPLAYER_DRAWER_H
