package com.icechn.videorecorder.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.icechn.videorecorder.R;
import com.icechn.videorecorder.client.RecorderClient;
import com.icechn.videorecorder.core.listener.IVideoChange;
import com.icechn.videorecorder.filter.image.DrawMultiImageFilter;
import com.icechn.videorecorder.filter.image.DrawMultiImageFilter.ImageDrawData;
import com.icechn.videorecorder.filter.softaudiofilter.SetVolumeAudioFilter;
import com.icechn.videorecorder.model.MediaConfig;
import com.icechn.videorecorder.model.RecordConfig;
import com.icechn.videorecorder.model.Size;

import java.util.ArrayList;


public class RecordingActivity extends AppCompatActivity implements
        TextureView.SurfaceTextureListener, View.OnClickListener, IVideoChange {
    public static final String IS_SQUARE = "is_square";
    protected RecorderClient mRecorderClient;
    protected AspectTextureView mTextureView;
    protected Handler mainHander;
    protected Button btn_toggle;
    protected boolean started;
    protected String mSaveVideoPath = null;
    protected boolean mIsSquare = false;
    RecordConfig recordConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent i = getIntent();
        mIsSquare = i.getBooleanExtra(IS_SQUARE, false);
        mSaveVideoPath = Environment.getExternalStorageDirectory().getPath() + "/live_save_video" + System.currentTimeMillis() + ".mp4";
        started = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);
        mTextureView = (AspectTextureView) findViewById(R.id.preview_textureview);
        mTextureView.setKeepScreenOn(true);
        mTextureView.setSurfaceTextureListener(this);

        btn_toggle = (Button) findViewById(R.id.btn_toggle);
        btn_toggle.setOnClickListener(this);

        findViewById(R.id.btn_swap).setOnClickListener(this);
        findViewById(R.id.btn_flash).setOnClickListener(this);

        prepareStreamingClient();
        onSetFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mainHander != null) {
            mainHander.removeCallbacksAndMessages(null);
        }
        if (started) {
            mRecorderClient.stopRecording();
        }
        if (mRecorderClient != null) {
            mRecorderClient.destroy();
        }
        super.onDestroy();
    }

    private void prepareStreamingClient() {
        mRecorderClient = new RecorderClient();

        recordConfig = RecordConfig.obtain();
        if (mIsSquare) {
            recordConfig.setTargetVideoSize(new Size(480, 480));
        } else {
            recordConfig.setTargetVideoSize(new Size(640, 480));
        }
        recordConfig.setSquare(true);
        recordConfig.setBitRate(750 * 1024);
        recordConfig.setVideoFPS(20);
        recordConfig.setVideoGOP(1);
        recordConfig.setRenderingMode(MediaConfig.Rending_Model_OpenGLES);
        //camera
        recordConfig.setDefaultCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        int frontDirection, backDirection;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, cameraInfo);
        frontDirection = cameraInfo.orientation;
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo);
        backDirection = cameraInfo.orientation;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recordConfig.setFrontCameraDirectionMode((frontDirection == 90 ? MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_270 : MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_90) | MediaConfig.DirectionMode.FLAG_DIRECTION_FLIP_HORIZONTAL);
            recordConfig.setBackCameraDirectionMode((backDirection == 90 ? MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_90 : MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_270));
        } else {
            recordConfig.setBackCameraDirectionMode((backDirection == 90 ? MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_0 : MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_180));
            recordConfig.setFrontCameraDirectionMode((frontDirection == 90 ? MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_180 : MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_0) | MediaConfig.DirectionMode.FLAG_DIRECTION_FLIP_HORIZONTAL);
        }
        //save video
        recordConfig.setSaveVideoPath(mSaveVideoPath);

        if (!mRecorderClient.prepare(this, recordConfig)) {
            mRecorderClient = null;
            Log.e("RecordingActivity", "prepare,failed!!");
            Toast.makeText(this, "StreamingClient prepare failed", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //resize textureview
        Size s = mRecorderClient.getVideoSize();
        mTextureView.setAspectRatio(AspectTextureView.MODE_INSIDE, ((double) s.getWidth()) / s.getHeight());

        mRecorderClient.setVideoChangeListener(this);

        mRecorderClient.setSoftAudioFilter(new SetVolumeAudioFilter());
    }

    protected void onSetFilters() {
        ArrayList<ImageDrawData> infos = new ArrayList<>();
        ImageDrawData data = new ImageDrawData();
        data.resId = R.drawable.t;
        data.rect = new Rect(100, 100, 238, 151);
        infos.add(data);
        mRecorderClient.setHardVideoFilter(new DrawMultiImageFilter(this, infos));
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        mTextureView.setAspectRatio(AspectTextureView.MODE_INSIDE, ((double) width) / height);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mRecorderClient != null) {
            mRecorderClient.startPreview(surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mRecorderClient != null) {
            mRecorderClient.updatePreview(width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mRecorderClient != null) {
            mRecorderClient.stopPreview(true);
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_toggle:
                if (!started) {
                    btn_toggle.setText("stop");
                    mRecorderClient.startRecording();
                } else {
                    btn_toggle.setText("start");
                    mRecorderClient.stopRecording();
                    Toast.makeText(RecordingActivity.this, "视频文件已保存至"+ mSaveVideoPath, Toast.LENGTH_SHORT).show();
                }
                started = !started;
                break;
            case R.id.btn_swap:
                mRecorderClient.swapCamera();
                findViewById(R.id.btn_flash).setVisibility(mRecorderClient.isFrontCamera() ? View.GONE : View.VISIBLE);
                break;
            case R.id.btn_flash:
                mRecorderClient.toggleFlashLight();
                break;
        }
    }


}
