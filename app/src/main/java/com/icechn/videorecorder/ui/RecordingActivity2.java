package com.icechn.videorecorder.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
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
import com.icechn.videorecorder.tools.TimeHandler;
import com.icechn.videorecorder.tools.VideoSplicer;

import java.io.File;
import java.util.ArrayList;


public class RecordingActivity2 extends AppCompatActivity implements
        TextureView.SurfaceTextureListener, View.OnClickListener, IVideoChange {
    private static final String TAG = "RecordingActivity2";
    public static final String IS_SQUARE = "is_square";
    private static final int MIN_RECORD_DURATION = 10000;//10S
    private static final int MAX_RECORD_DURATION = 30000;//30S
    protected RecorderClient mRecorderClient;
    protected AspectTextureView mTextureView;
    protected Handler mainHander;
    private TextView mTipTextView;
    private ProgressBar mTimeProgressBar;
    private TextView mTimeView;
    protected String mSaveVideoPath = null;
    protected boolean mIsSquare = false;
    RecordConfig recordConfig;

    private static Rect getScreenBounds(Context context) {
        if (context == null) {
            return null;
        }
        Rect rect = new Rect();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        rect.set(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        return rect;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent i = getIntent();
        mIsSquare = i.getBooleanExtra(IS_SQUARE, false);
        mSaveVideoPath = Environment.getExternalStorageDirectory().getPath() + "/live_save_video" + System.currentTimeMillis() + ".mp4";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        mTextureView = (AspectTextureView) findViewById(R.id.preview_textureview);
        mTextureView.setKeepScreenOn(true);
        mTextureView.setSurfaceTextureListener(this);

        mTimeProgressBar = (ProgressBar) findViewById(R.id.pb_timeline);
        mTimeProgressBar.setMax(MAX_RECORD_DURATION);
        mTimeProgressBar.setProgress(0);
        mTipTextView = (TextView) findViewById(R.id.tv_tips);
        mTimeView = (TextView) findViewById(R.id.timeview);

        Rect rect = getScreenBounds(getApplicationContext());
        int sWidth = rect.width();
        ((LayoutParams)(findViewById(R.id.divide_view)).getLayoutParams()).leftMargin =
                sWidth * MIN_RECORD_DURATION / MAX_RECORD_DURATION;

        findViewById(R.id.btn_swap).setOnClickListener(this);
        findViewById(R.id.btn_flash).setOnClickListener(this);
        findViewById(R.id.btn_del).setOnClickListener(this);
        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_cap).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){//按下
                    resumeRecording();
                }else if(event.getAction()==MotionEvent.ACTION_UP){//抬起
                    pauseRecording();
                }
                return false;
            }
        });
        findViewById(R.id.btn_del).setEnabled(mFileIdx > 0);

        prepareStreamingClient();
        onSetFilters();

        mTimeHandle = new TimeHandler(Looper.getMainLooper(), mTimeTask);

        mp4List = new ArrayList<>();
        durationList = new ArrayList<>();
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
        hideProgressDialog();
        if (mainHander != null) {
            mainHander.removeCallbacksAndMessages(null);
        }
        if (isRecording) {
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

    private String getTmpFolderPath() {
        File dic = new File(Environment.getExternalStorageDirectory().getPath() + "/tmpVideoRecord/");
        if(!dic.exists()){
            dic.mkdirs();
        }
        return dic.toString();
    }

    private void resumeRecording() {
        if (totalTime >= MAX_RECORD_DURATION) {
            Log.i(TAG, "already full");
            return ;
        }
        mFileIdx++;
        mRecorderClient.updatePath(getTmpFolderPath() + "/"+mFileIdx+".mp4");
        mRecorderClient.startRecording();
        isRecording = true;
        mStartTime = System.currentTimeMillis();
        mTimeHandle.sendLoopMsg(0L, 100L);
    }
    private void pauseRecording() {
        if (!isRecording) {
            return ;
        }
        mRecorderClient.stopRecording();
        isRecording = false;
        stopTimeTask();

        String path = mRecorderClient.getFilePath();
        if (mCurrentDuration > 1000) {
            if (!TextUtils.isEmpty(path)) {
                totalTime += mCurrentDuration;
                durationList.add(mCurrentDuration);
                mp4List.add(path);
                findViewById(R.id.btn_del).setEnabled(mFileIdx > 0);
                return ;
            }
        }
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
        mFileIdx--;
        mTimeProgressBar.setProgress((int) (totalTime));
        mTimeView.setText((totalTime)/1000+"s");
        if (totalTime < MIN_RECORD_DURATION) {
            findViewById(R.id.btn_ok).setEnabled(false);
        }
        findViewById(R.id.btn_del).setEnabled(mFileIdx > 0);
    }

    private boolean isRecording = false;
    private int mFileIdx = 0;
    private ArrayList<String> mp4List;
    private ArrayList<Long> durationList;

    private void mergeFile() {
        finishRecording(mSaveVideoPath);
    }

    public void finishRecording(String filePath) {
        mFileIdx = 0;
        File file = new File(filePath);
        if(file.exists()){
            file.delete();
        }
        if(mp4List.size() == 1) {
            new File(mp4List.get(0)).renameTo(new File(mSaveVideoPath));
            Toast.makeText(getApplicationContext(), "视频文件已保存至"+ mSaveVideoPath, Toast.LENGTH_SHORT).show();
            mp4List.clear();
            durationList.clear();
            finish();
            return;
        }

        new Mp4MergeTask(mp4List, mSaveVideoPath, new IMergeListener() {
            @Override
            public void onMergeBegin(boolean success) {
                showProgressDialog();
            }

            @Override
            public void onMergeEnd(boolean success) {
                hideProgressDialog();
                if (success) {
                    Toast.makeText(getApplicationContext(), "视频文件已保存至" + mSaveVideoPath, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "视频文件保存失败!!!", Toast.LENGTH_SHORT).show();
                }
                mp4List.clear();
                durationList.clear();
                finish();
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private Dialog mDialog;
    private void showProgressDialog() {
        Dialog dialog = new Dialog(RecordingActivity2.this);
        dialog.setContentView(new ProgressBar(RecordingActivity2.this));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle("正在生成视频文件...");
        mDialog = dialog;
        dialog.show();
    }
    private void hideProgressDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void fallBack() {
        if(mFileIdx > 0) {
            mFileIdx--;
        }
        if(mp4List.size()>0) {
            String path = mp4List.remove(mp4List.size() - 1);
            File file = new File(path);
            if(file.exists()){
                file.delete();
            }
            Long time = durationList.remove(durationList.size() - 1);
            totalTime -= time.longValue();
            mTimeProgressBar.setProgress((int) (totalTime));
            mTimeView.setText((totalTime)/1000+"s");
            if (totalTime < MIN_RECORD_DURATION) {
                findViewById(R.id.btn_ok).setEnabled(false);
            }
            findViewById(R.id.btn_del).setEnabled(mFileIdx > 0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_del:
                if (isRecording) {
                    return ;
                }
                fallBack();
                break;
            case R.id.btn_ok:
                if (isRecording) {
                    return ;
                }
                mergeFile();
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

    private TimeHandler mTimeHandle;
    private long mCurrentDuration = 0;//ms
    private long totalTime = 0;
    private long mStartTime = 0;//ms
    private TimeHandler.Task mTimeTask = new TimeHandler.Task() {
        @Override
        public void run() {
            long timeLapse = System.currentTimeMillis() - mStartTime;
            mCurrentDuration = timeLapse;
            Log.i(TAG, "duration="+mCurrentDuration);
            mTimeProgressBar.setProgress((int) (totalTime + timeLapse));
            mTimeView.setText((totalTime + timeLapse)/1000+"s");
            if ((totalTime + timeLapse) >= MAX_RECORD_DURATION) {
                pauseRecording();
            } else if ((totalTime + timeLapse) >= MIN_RECORD_DURATION) {
                findViewById(R.id.btn_ok).setEnabled(true);
            } else {
                findViewById(R.id.btn_ok).setEnabled(false);
            }
        }
    };
    private void stopTimeTask() {
        mTimeHandle.clearMsg();
    }

    private static class Mp4MergeTask extends AsyncTask<Object, Object, Boolean> {

        private ArrayList<String> list;
        private String path;
        private IMergeListener listener;
        public Mp4MergeTask(ArrayList<String> uriList, String outputPath, IMergeListener listener) {
            list = uriList;
            path = outputPath;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (listener != null) {
                listener.onMergeBegin(true);
            }
        }

        @Override
        protected Boolean doInBackground(Object... objects) {
            boolean ret = new VideoSplicer(list, path).joinVideo();

            return ret;
        }

        @Override
        protected void onPostExecute(Boolean o) {
            super.onPostExecute(o);
            if (listener != null) {
                listener.onMergeEnd(o);
            }
        }
    }
    private static interface IMergeListener {
        void onMergeBegin(boolean success);
        void onMergeEnd(boolean success);
    }
}
