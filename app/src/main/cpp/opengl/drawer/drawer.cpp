//
// Created by mruilab on 2022/4/8.
//

#include <malloc.h>
#include "drawer.h"
#include "logger.h"

Drawer::Drawer(int origin_width, int origin_height) :
        m_origin_width(origin_width),
        m_origin_height(origin_height) {

}

Drawer::~Drawer() {

}

void Drawer::SetVideoSize(int width, int height) {
    this->m_origin_width = width;
    this->m_origin_height = height;
}

void Drawer::SetDisplaySize(int width, int height) {
    if (this->m_display_width != width ||
        this->m_display_width != height) {
        this->m_display_width = width;
        this->m_display_height = height;
        m_display_size_change = true;
    }
}

void Drawer::SetTextureNum(int num) {
    this->m_texture_num = num;
}

void Drawer::Draw() {
    if (IsReadyToDraw()) {
        CreateTextureId();
        UpdateMVPMatrix();
        CreateProgram();
        BindTexture();  //子类实现
        PrepareDraw();  //子类实现
        DoDraw();
        DoneDraw();
    }
}

bool Drawer::IsReadyToDraw() {
    return m_origin_width > 0 && m_origin_height > 0 && m_texture_num > 0;
}

void Drawer::CreateTextureId() {
    if (!is_create_texture_id) {
        glGenTextures(m_texture_num, m_texture_ids);
        LOGI(TAG, "Create texture id : %d, %x", m_texture_num, glGetError())
        if (glGetError() == 0)
            is_create_texture_id = true;
    }
}

void Drawer::UpdateMVPMatrix() {
    if (m_matrix != NULL && !m_display_size_change)return;
    int dstWidth = m_display_width;
    int dstHeight = dstWidth * m_origin_height / m_origin_width;
    if (dstHeight > m_display_height) {
        dstWidth = m_display_height * m_origin_width / m_origin_height;
        float scale = (float) dstWidth / m_display_width;
        m_transform = glm::scale(glm::mat4(1.0f), glm::vec3(scale, 1, 1));
    } else {
        float scale = (float) dstHeight / m_display_height;
        m_transform = glm::scale(glm::mat4(1.0f), glm::vec3(1, scale, 1));
    }
    m_matrix = glm::value_ptr(m_transform);
    m_display_size_change = false;
}

void Drawer::CreateProgram() {
    if (m_program_id == 0) {
        //创建一个空的OpenGLES程序，注意：需要在OpenGL渲染线程中创建，否则无法渲染
        m_program_id = glCreateProgram();
        LOGI(TAG, "create gl program : %d, %x", m_program_id, glGetError())
        if (glGetError() != GL_NO_ERROR) {
            return;
        }

        GLuint vertexShader = LoadShader(GL_VERTEX_SHADER, GetVertexShader());
        GLuint fragmentShader = LoadShader(GL_FRAGMENT_SHADER, GetFragmentShader());

        //将顶点着色器加入到程序
        glAttachShader(m_program_id, vertexShader);
        //将片元着色器加入到程序中
        glAttachShader(m_program_id, fragmentShader);
        //连接到着色器程序
        glLinkProgram(m_program_id);

        m_vertex_matrix_handler = glGetUniformLocation(m_program_id, "u_MVPMatrix");
        m_vertex_pos_handler = glGetAttribLocation(m_program_id, "a_position");
        m_texture_pos_handler = glGetAttribLocation(m_program_id, "a_texCoord");

        InitCstShaderHandler();

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }
    //使用OpenGL程序
    if (m_program_id != 0) {
        glUseProgram(m_program_id);
    }
}

GLuint Drawer::LoadShader(GLenum type, const GLchar *shader_code) {
    LOGI(TAG, "Load shader:\n %s", shader_code)
    //根据type创建顶点着色器或者片元着色器
    GLuint shader = glCreateShader(type);
    //将资源加入到着色器中，并编译
    glShaderSource(shader, 1, &shader_code, NULL);
    glCompileShader(shader);

    GLint compiled;
    // 检查编译状态
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (!compiled) {
        GLint infoLen = 0;

        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);

        if (infoLen > 1) {
            GLchar *infoLog = (GLchar *) malloc(sizeof(GLchar) * infoLen);

            glGetShaderInfoLog(shader, infoLen, NULL, infoLog);
            LOGI(TAG, "Error compiling shader:\n%s\n", infoLog);

            free(infoLog);
        }

        glDeleteShader(shader);
        return 0;
    }
    return shader;
}

void Drawer::ActivateTexture(GLenum index, GLenum type) {
    //激活指定纹理单元
    glActiveTexture(GL_TEXTURE0 + index);
    //绑定纹理ID到纹理单元
    glBindTexture(type, m_texture_ids[index]);
    //将活动的纹理单元传递到着色器里面
    glUniform1i(index, index);

    //配置边缘过渡参数
    glTexParameterf(type, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(type, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(type, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(type, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
}

void Drawer::DoDraw() {
    //启用顶点的句柄
    glEnableVertexAttribArray(m_vertex_pos_handler);
    glEnableVertexAttribArray(m_texture_pos_handler);
    //设置着色器参数
    glUniformMatrix4fv(m_vertex_matrix_handler, 1, GL_FALSE, m_matrix);
    glVertexAttribPointer(m_vertex_pos_handler, 2, GL_FLOAT, GL_FALSE, 0, m_vertex_coors);
    glVertexAttribPointer(m_texture_pos_handler, 2, GL_FLOAT, GL_FALSE, 0, m_texture_coors);
    //开始绘制
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
}

void Drawer::Release() {
    glDisableVertexAttribArray(m_vertex_pos_handler);
    glDisableVertexAttribArray(m_texture_pos_handler);
    glBindTexture(GL_TEXTURE_2D, 0);
    glDeleteTextures(m_texture_num, m_texture_ids);
    is_create_texture_id = false;
    glDeleteProgram(m_program_id);
}
