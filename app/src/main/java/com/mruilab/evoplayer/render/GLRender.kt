package com.mruilab.evoplayer.render

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.mruilab.evoplayer.utils.ColorFormat
import com.mruilab.evoplayer.utils.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRender : GLSurfaceView.Renderer {

    private var mContext: Context

    private var mDisplayWidth: Int = 0
    private var mDisplayHeight: Int = 0

    private var mProgram: Int = 0
    private lateinit var mTextureIds: IntArray

    private var mColorFormat: ColorFormat = ColorFormat.UNKNOWN

    private var yuvWidth: Int = 0
    private var yuvHeight: Int = 0

    private var yBuffer: ByteBuffer? = null
    private var uBuffer: ByteBuffer? = null
    private var vBuffer: ByteBuffer? = null
    private var uvBuffer: ByteBuffer? = null

    private var yBytes: ByteArray? = null
    private var uBytes: ByteArray? = null
    private var vBytes: ByteArray? = null
    private var uvBytes: ByteArray? = null

    lateinit var mVertexBuffer: FloatBuffer

    var mVertexMatrixHandler: Int = 0

    private var hasInit = false

    private val mDefMatrix: FloatArray = floatArrayOf(
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, -1.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 1.0f
    )

    constructor(context: Context) {
        mContext = context
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mDisplayWidth = width
        mDisplayHeight = height
        // 视距区域设置使用 GLSurfaceView 的宽高
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (yBuffer == null) return
        if ((mColorFormat == ColorFormat.I420 ||
                    mColorFormat == ColorFormat.YV12) &&
            (uBuffer == null || vBuffer == null)
        ) return
        if ((mColorFormat == ColorFormat.NV12 ||
                    mColorFormat == ColorFormat.NV21 ||
                    mColorFormat == ColorFormat.NV21_32M) &&
            uvBuffer == null
        ) return

        if (!hasInit) {
            init()
            hasInit = true
        }
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT) // clear color buffer
        // 1. 选择使用的程序
        GLES30.glUseProgram(mProgram)
        // 2.1 加载纹理y
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0) //激活纹理0
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureIds[0]) //绑定纹理
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, yuvWidth,
            yuvHeight, 0, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, yBuffer
        ) // 赋值
        GLES30.glUniform1i(0, 0) // sampler_y的location=0, 把纹理0赋值给sampler_y
        if (mColorFormat == ColorFormat.I420 ||
            mColorFormat == ColorFormat.YV12
        ) {
            // 2.2 加载纹理u
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureIds[1])
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, yuvWidth / 2,
                yuvHeight / 2, 0, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, uBuffer
            )
            GLES30.glUniform1i(1, 1) // sampler_u的location=1, 把纹理1赋值给sampler_u
            // 2.3 加载纹理v
            GLES30.glActiveTexture(GLES30.GL_TEXTURE2)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureIds[2])
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, yuvWidth / 2,
                yuvHeight / 2, 0, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, vBuffer
            )
            GLES30.glUniform1i(2, 2) // sampler_v的location=2, 把纹理1赋值给sampler_v
        } else {
            // 2.2 加载纹理uv
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureIds[1])
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE_ALPHA, yuvWidth / 2,
                yuvHeight / 2, 0, GLES30.GL_LUMINANCE_ALPHA, GLES30.GL_UNSIGNED_BYTE, uvBuffer
            )
            GLES30.glUniform1i(1, 1)
        }
        // 3. 加载顶点数据
        mVertexBuffer.position(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 5 * 4, mVertexBuffer)
        GLES30.glEnableVertexAttribArray(0)
        mVertexBuffer.position(3)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 5 * 4, mVertexBuffer)
        GLES30.glEnableVertexAttribArray(1)
        modifyMatrix()
        GLES30.glUniformMatrix4fv(mVertexMatrixHandler, 1, false, mDefMatrix, 0)
        // 4. 绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6)
    }

    private fun init() {
        val vertexSource = ShaderUtils.loadFromAssets("vertex.vsh", mContext.resources)
        val fragmentSource =
            if (mColorFormat == ColorFormat.I420 || mColorFormat == ColorFormat.YV12) {
                ShaderUtils.loadFromAssets("yuv420p_fragment.fsh", mContext.resources);
            } else {
                ShaderUtils.loadFromAssets("yuv420sp_fragment.fsh", mContext.resources);
            }
        mProgram = ShaderUtils.createProgram(vertexSource, fragmentSource)
        mVertexMatrixHandler = GLES30.glGetUniformLocation(mProgram, "uMatrix")
        //创建纹理
        mTextureIds =
            if (mColorFormat == ColorFormat.I420 || mColorFormat == ColorFormat.YV12) {
                IntArray(3)
            } else {
                IntArray(2)
            }
        GLES30.glGenTextures(mTextureIds.size, mTextureIds, 0)
        for(textureId in mTextureIds) {
            //绑定纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
            //设置环绕和过滤方式
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT)
            GLES30.glTexParameteri(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER,
                GLES30.GL_LINEAR
            )
            GLES30.glTexParameteri(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER,
                GLES30.GL_LINEAR
            )
        }

        val vertices = floatArrayOf(
            // 前三个数字为顶点坐标(x, y, z)，后两个数字为纹理坐标(s, t)
            // 第一个三角形
            1f, 1f, 0f, 1f, 0f,
            1f, -1f, 0f, 1f, 1f,
            -1f, -1f, 0f, 0f, 1f,
            // 第二个三角形
            1f, 1f, 0f, 1f, 0f,
            -1f, -1f, 0f, 0f, 1f,
            -1f, 1f, 0f, 0f, 0f
        )
        val vbb = ByteBuffer.allocateDirect(vertices.size * 4) // 一个 float 是四个字节
        vbb.order(ByteOrder.nativeOrder()) // 必须要是 native order
        mVertexBuffer = vbb.asFloatBuffer()
        mVertexBuffer.put(vertices)
    }

    private fun modifyMatrix() {
        val height = (yuvHeight * mDisplayWidth / yuvWidth).toFloat()
        mDefMatrix[5] = height / mDisplayHeight
    }

    fun setYUVData(yuv: ByteArray, width: Int, height: Int, colorFormat: ColorFormat) {
        mColorFormat = colorFormat
        yBuffer?.clear()
        uBuffer?.clear()
        vBuffer?.clear()
        uvBuffer?.clear()

        if (yBytes == null) yBytes = ByteArray(width * height)
        System.arraycopy(yuv, 0, yBytes, 0, yBytes!!.size)
        yBuffer = ByteBuffer.wrap(yBytes)

        if (mColorFormat == ColorFormat.I420 || mColorFormat == ColorFormat.YV12) {
            if (uBytes == null) uBytes = ByteArray(width * height / 4)
            System.arraycopy(yuv, yBytes!!.size, uBytes, 0, uBytes!!.size)
            uBuffer = ByteBuffer.wrap(uBytes)
            if (vBytes == null) vBytes = ByteArray(width * height / 4)
            System.arraycopy(yuv, yBytes!!.size + uBytes!!.size, vBytes, 0, vBytes!!.size)
            vBuffer = ByteBuffer.wrap(vBytes)
        } else {
            if (uvBytes == null) uvBytes = ByteArray(width * height / 2)
            System.arraycopy(yuv, yBytes!!.size, uvBytes, 0, uvBytes!!.size)
            uvBuffer = ByteBuffer.wrap(uvBytes)
        }

        yuvWidth = width
        yuvHeight = height
    }
}