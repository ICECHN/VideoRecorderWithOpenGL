package com.icechn.videorecorder.filter.hardvideofilter;

import android.content.Context;
import android.opengl.GLES20;

import com.icechn.videorecorder.tools.GLESTools;


/**
 *
 * sigma = 0.1.Optimized Selective Gaussian Blur.
 */
public class SkinBlurHardVideoFilter extends OriginalHardVideoFilter {
    private int xStepLoc;
    private int yStepLoc;
    private float stepScale;

    /**
     * @param stepScale suggest:480P = 2,720P = 3
     */
    public SkinBlurHardVideoFilter(Context context, int stepScale) {
        super(null, GLESTools.uRes(context.getResources(), "skinblur_fragment.sh"));
        this.stepScale = (float) stepScale;
    }

    @Override
    public void onInit(int VWidth, int VHeight) {
        super.onInit(VWidth, VHeight);
        yStepLoc = GLES20.glGetUniformLocation(glProgram, "yStep");
        xStepLoc = GLES20.glGetUniformLocation(glProgram, "xStep");
    }

    @Override
    protected void onPreDraw() {
        super.onPreDraw();
        GLES20.glUniform1f(xStepLoc, (float) (stepScale / outVideoWidth));
        GLES20.glUniform1f(yStepLoc, (float) (stepScale / outVideoHeight));
    }
}
