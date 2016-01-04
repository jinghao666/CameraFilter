package me.relex.camerafilter.filter.BlurFilter;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLES10;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import me.relex.camerafilter.R;
import me.relex.camerafilter.filter.CameraFilter;
import me.relex.camerafilter.gles.GlUtil;

public class CameraFilterMotionBlur extends CameraFilter {

    protected int muSamplerStepLoc;
    protected int mublurNormLoc;
    protected int musamplerRadiusLoc;

    private static float SAMPLER_STEP[] = {0.02f, 0.02f};
    private static float BLUR_NORM[] = {1.0f, 0.0f};
    private static float SAMPLER_REDIUS = 3.0f;

    private static FloatBuffer SAMPLER_STEP_BUF;
    private static final FloatBuffer BLUR_NORM_BUF = GlUtil.createFloatBuffer(BLUR_NORM);

    public CameraFilterMotionBlur(Context context) {
        this(context, 0.02f, 3.0f);
    }

    public CameraFilterMotionBlur(Context context, float sampler_step, float sampler_redius) {
        super(context);

        SAMPLER_STEP    = new float[]{sampler_step, sampler_step};
        SAMPLER_REDIUS  = sampler_redius;

        SAMPLER_STEP_BUF = GlUtil.createFloatBuffer(SAMPLER_STEP);

    }

    @Override protected void getGLSLValues() {
        super.getGLSLValues();

        muSamplerStepLoc    = GLES20.glGetUniformLocation(mProgramHandle, "samplerStep");
        mublurNormLoc       = GLES20.glGetUniformLocation(mProgramHandle, "blurNorm");
        musamplerRadiusLoc  = GLES20.glGetUniformLocation(mProgramHandle, "samplerRadius");
    }

    @Override
    protected void bindGLSLValues(float[] mvpMatrix, FloatBuffer vertexBuffer, int coordsPerVertex,
                                  int vertexStride, float[] texMatrix, FloatBuffer texBuffer, int texStride) {

        super.bindGLSLValues(mvpMatrix, vertexBuffer, coordsPerVertex,
                vertexStride, texMatrix, texBuffer, texStride);

        GLES20.glUniform2fv(muSamplerStepLoc, 1, SAMPLER_STEP_BUF);
        GLES20.glUniform2fv(mublurNormLoc, 1, BLUR_NORM_BUF);
        GLES20.glUniform1f(musamplerRadiusLoc, SAMPLER_REDIUS);
    }

    @Override protected int createProgram(Context applicationContext) {
        return GlUtil.createProgram(applicationContext, R.raw.vertex_shader,
                R.raw.fragment_shader_ext_motionblur);
    }

}