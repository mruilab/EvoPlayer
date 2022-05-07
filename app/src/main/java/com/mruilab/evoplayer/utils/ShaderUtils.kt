package com.mruilab.evoplayer.utils

import android.content.res.Resources
import android.opengl.GLES30
import android.util.Log

class ShaderUtils {

    companion object {
        const val TAG = "ShaderUtils"

        fun loadShader(type: Int, source: String): Int {
            // 1.create shader
            var shader = GLES30.glCreateShader(type)
            if (shader == GLES30.GL_NONE) {
                Log.e(TAG, "create shared failed! type: $type")
                return GLES30.GL_NONE
            }
            // 2.load shader source
            GLES30.glShaderSource(shader, source)
            // 3.compile shader source
            GLES30.glCompileShader(shader)
            // 4.check compile status
            val compiled = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == GLES30.GL_FALSE) { // compile failed
                Log.e(TAG, "Error compiling shader. type: $type:")
                Log.e(TAG, GLES30.glGetShaderInfoLog(shader))
                GLES30.glDeleteShader(shader) //delete shader
                shader = GLES30.GL_NONE
            }
            return shader
        }

        fun createProgram(vertexSource: String, fragmentSource: String): Int {
            // 1.load shader
            val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource)
            if (vertexShader == GLES30.GL_NONE) {
                Log.e(TAG, "load vertex shader failed! ")
                return GLES30.GL_NONE
            }
            val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
            if (fragmentShader == GLES30.GL_NONE) {
                Log.e(TAG, "load fragment shader failed! ")
                return GLES30.GL_NONE
            }
            // 2.create gl program
            val program = GLES30.glCreateProgram()
            if (program == GLES30.GL_NONE) {
                Log.e(TAG, "create program failed! ")
                return GLES30.GL_NONE
            }
            // 3.attach shader
            GLES30.glAttachShader(program, vertexShader)
            GLES30.glAttachShader(program, fragmentShader)
            // we can delete shader after attach
            GLES30.glDeleteShader(vertexShader)
            GLES30.glDeleteShader(fragmentShader)
            // 4. link program
            GLES30.glLinkProgram(program)
            // 5. check link status
            val linkStatus = IntArray(1)
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == GLES30.GL_FALSE) { // link failed
                Log.e(TAG, "Error link program: ")
                Log.e(TAG, GLES30.glGetProgramInfoLog(program))
                GLES30.glDeleteProgram(program) // delete program
                return GLES30.GL_NONE
            }
            return program
        }

        fun loadFromAssets(fileName: String, resources: Resources): String {
            var ret: String?
            val inputStream = resources.assets.open(fileName)
            val len = inputStream.available()
            val data = ByteArray(len)
            inputStream.read(data)
            inputStream.close()
            ret = String(data)
            ret.replace("\\r\\n", "\\n")
            return ret
        }
    }

}