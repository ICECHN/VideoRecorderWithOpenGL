package com.icechn.videorecorder.filter.hardvideofilter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.icechn.videorecorder.tools.GLESTools;

import java.nio.FloatBuffer;

/**
 * Created by lake on 20/02/17.
 * libREStreaming project.
 */

public class GaussianBlurHardFilter extends BaseHardVideoFilter {
    private int blurRadius;
    protected int glProgram;
    protected int glTextureLoc;
    protected int glCamPostionLoc;
    protected int glCamTextureCoordLoc;
    protected int glStepLoc;
    protected int glIgnoreRect;
    protected String vertexShader_filter = "";
    protected String fragmentshader_filter = "";

    public GaussianBlurHardFilter(Context context, int blurRadius) {
        this.blurRadius = blurRadius;
        this.vertexShader_filter = GLESTools.uRes(context.getResources(), "gaussian_vertex.sh");
        this.fragmentshader_filter = GLESTools.uRes(context.getResources(), "gaussian_fragment.sh");
    }

    @Override
    public void onInit(int videoWidth, int videoHeight) {
        super.onInit(videoWidth, videoHeight);
        String fragShader = "#define GAUSSIAN_BLUR_RADIUS "+blurRadius+".\n"+fragmentshader_filter;
        glProgram = GLESTools.createProgram(vertexShader_filter, fragShader);
        GLES20.glUseProgram(glProgram);
        glTextureLoc = GLES20.glGetUniformLocation(glProgram, "uCamTexture");
        glCamPostionLoc = GLES20.glGetAttribLocation(glProgram, "aCamPosition");
        glCamTextureCoordLoc = GLES20.glGetAttribLocation(glProgram, "aCamTextureCoord");
        glStepLoc = GLES20.glGetUniformLocation(glProgram,"step");
        glIgnoreRect = GLES20.glGetUniformLocation(glProgram,"ignoreRect");
    }

    @Override
    public void onDraw(int cameraTexture, int targetFrameBuffer, FloatBuffer shapeBuffer, FloatBuffer textureBuffer) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, targetFrameBuffer);
        GLES20.glUseProgram(glProgram);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTexture);
        GLES20.glUniform1i(glTextureLoc, 0);
        GLES20.glUniform2f(glStepLoc,1f/ outVideoWidth,0f);
        GLES20.glEnableVertexAttribArray(glCamPostionLoc);
        GLES20.glEnableVertexAttribArray(glCamTextureCoordLoc);
        shapeBuffer.position(0);
        GLES20.glVertexAttribPointer(glCamPostionLoc, 2,
                GLES20.GL_FLOAT, false,
                2 * 4, shapeBuffer);
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(glCamTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false,
                2 * 4, textureBuffer);
        GLES20.glViewport(0, 0, outVideoWidth, outVideoHeight);
        GLES20.glUniform1i(glTextureLoc, 0);
        GLES20.glUniform2f(glStepLoc,0f,1f/ outVideoHeight);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawIndecesBuffer.limit(), GLES20.GL_UNSIGNED_SHORT, drawIndecesBuffer);
        GLES20.glFinish();

        GLES20.glDisableVertexAttribArray(glCamPostionLoc);
        GLES20.glDisableVertexAttribArray(glCamTextureCoordLoc);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteProgram(glProgram);
    }
}
