package com.icechn.videorecorder.filter.gpuimage;

/**
 * Created by ICE on 2018/2/2.
 */

public class GPUImageColorInvertFilter extends GPUImageFilter {
    public static final String COLOR_INVERT_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "    \n" +
            "    gl_FragColor = vec4((1.0 - textureColor.rgb), textureColor.w);\n" +
            "}";

    public GPUImageColorInvertFilter() {
        super(NO_FILTER_VERTEX_SHADER, COLOR_INVERT_FRAGMENT_SHADER);
    }
}
