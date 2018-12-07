/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// from: https://android.googlesource.com/platform/cts/+/lollipop-release/tests/tests/media/src/android/media/cts/TextureRender.java
// blob: 4125dcfcfed6ed7fddba5b71d657dec0d433da6a
// modified: removed unused method bodies
// modified: use GL_LINEAR for GL_TEXTURE_MIN_FILTER to improve quality.
package net.ypresto.androidtranscoder.engine;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import net.ypresto.androidtranscoder.TLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;

/**
 * Code for rendering a texture onto a surface using OpenGL ES 2.0.
 */
class TextureRender {
    private static final String TAG = "TextureRender";
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f,  1.0f, 0, 0.f, 1.f,
            1.0f,  1.0f, 0, 1.f, 1.f,
    };
    private FloatBuffer mTriangleVertices;
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";
    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +      // highp here doesn't seem to matter
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture1;\n" +
                    "uniform samplerExternalOES sTexture2;\n" +
                    "uniform samplerExternalOES sTexture3;\n" +
                    "uniform samplerExternalOES sTexture4;\n" +
                    "uniform float uAlpha1;\n" +
                    "uniform float uAlpha2;\n" +
                    "uniform float uAlpha3;\n" +
                    "uniform float uAlpha4;\n" +
                    "void main() {\n" +
                    "  if (uAlpha1 >= 0.00) {\n" +
                    "      gl_FragColor = texture2D(sTexture1, vTextureCoord) * uAlpha1;\n" +
                    "  }\n" +
                    "  if (uAlpha2 >= 0.00) {\n" +
                    "      gl_FragColor += texture2D(sTexture2, vTextureCoord) * uAlpha2;\n" +
                    "  }\n" +
                    "}\n";
    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];
    private int mProgram;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;
    private int [] muTextures = new int[4];
    private int [] muAlphas = new int[4];

    List<OutputSurface> mOutputSurfaces;
    static int mGLESTextures [] = {GLES20.GL_TEXTURE0, GLES20.GL_TEXTURE1, GLES20.GL_TEXTURE2, GLES20.GL_TEXTURE3, GLES20.GL_TEXTURE4};


    public TextureRender(List<OutputSurface> outputSurfaces) {
        mOutputSurfaces = outputSurfaces;
        mTriangleVertices = ByteBuffer.allocateDirect(
                mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);
        Matrix.setIdentityM(mSTMatrix, 0);
    }

    public void drawFrame() {

        checkGlError("onDrawFrame start");

        OutputSurface outputSurface = mOutputSurfaces.get(0);
        SurfaceTexture surfaceTexture = outputSurface.getSurfaceTexture();
        surfaceTexture.getTransformMatrix(mSTMatrix);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        int textures =0;
        for (int textureIndex = 0; textureIndex < mOutputSurfaces.size(); ++textureIndex) {
            if (mOutputSurfaces.get(textureIndex).getRotation() == outputSurface.getRotation()) {
                GLES20.glUniform1i(muTextures[textureIndex], textureIndex);
                GLES20.glActiveTexture(mGLESTextures[textureIndex]);
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOutputSurfaces.get(textureIndex).getTextureID());
                GLES20.glUniform1f(muAlphas[textureIndex], mOutputSurfaces.get(textureIndex).getAlpha());
                ++textures;
            }
        }
        if (textures < 2)
            GLES20.glUniform1f(muAlphas[0], 1.0f);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");
        Matrix.setIdentityM(mMVPMatrix, 0);

        //if (mOutputSurfaces.get(0).getFlip())
        //    Matrix.rotateM(mMVPMatrix, 0, 180, 0, 0, 1);

        float ratioX = outputSurface.getSourceRect().width() / outputSurface.getDestRect().width();
        float ratioY = outputSurface.getSourceRect().height() / outputSurface.getDestRect().height();
        float offset = (outputSurface.getDestRect().width() - outputSurface.getSourceRect().width()) / 2;
        if (ratioX != 1)
            Matrix.scaleM(mMVPMatrix,  0,ratioX / 2.0f, ratioY / 2.0f, 1);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
        //GLES20.glEnable(GLES20.GL_BLEND);
        //GLES20.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");
        GLES20.glFinish();

        for (int textureIndex = 0; textureIndex < mOutputSurfaces.size(); ++textureIndex) {
            mOutputSurfaces.get(textureIndex).clearTextureReady();
        }

    }
    /**
     * Initializes GL state.  Call this after the EGL surface has been created and made current.
     */
    public void surfaceCreated() {
        mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (mProgram == 0) {
            throw new RuntimeException("failed creating program");
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }
        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        checkGlError("glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uSTMatrix");
        }
        for (int textureIndex = 0; textureIndex < mOutputSurfaces.size(); ++textureIndex) {
            muTextures[textureIndex] = GLES20.glGetUniformLocation(mProgram, "sTexture" + (textureIndex + 1));
            checkGlError("glGetUniformLocation sTexture");
            if (muTextures[textureIndex] == -1) {
                throw new RuntimeException("Could not get attrib location for sTexture" + (textureIndex + 1));
            }
        }
        for (int textureIndex = 0; textureIndex < mOutputSurfaces.size(); ++textureIndex) {
            muAlphas[textureIndex] = GLES20.glGetUniformLocation(mProgram, "uAlpha" + (textureIndex + 1));
            checkGlError("glGetUniformLocation uALpha");
            if (muTextures[textureIndex] == -1) {
                throw new RuntimeException("Could not get attrib location for uAlpha" + (textureIndex + 1));
            }
        }

        for (int textureIndex = 0; textureIndex < mOutputSurfaces.size(); ++textureIndex) {
            GLES20.glActiveTexture(mGLESTextures[textureIndex]);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOutputSurfaces.get(textureIndex).getTextureID());
            checkGlError("glBindTexture inTexture1");
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
            checkGlError("glTexParameter");
        }

    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glClearColor(0,0,0, 1);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            TLog.e(TAG, "Could not compile shader " + shaderType + ":");
            TLog.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }
    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }
        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0) {
            TLog.e(TAG, "Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            TLog.e(TAG, "Could not link program: ");
            TLog.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }
    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            TLog.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
    /**
     * Saves the current frame to disk as a PNG image.  Frame starts from (0,0).
     * <p>
     * Useful for debugging.
     */
    public static void saveFrame(String filename, int width, int height) {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
