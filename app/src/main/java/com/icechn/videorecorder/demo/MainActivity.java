package com.icechn.videorecorder.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.icechn.videorecorder.R;
import com.icechn.videorecorder.ui.RecordingActivity;
import com.icechn.videorecorder.ui.RecordingActivity2;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_STREAM = 1;
    private static String[] PERMISSIONS_STREAM = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    boolean authorized = false;
    boolean continuedRecord = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        continuedRecord = ((CheckBox)findViewById(R.id.ck_duandian)).isChecked();
        ((CheckBox)findViewById(R.id.ck_duandian)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                continuedRecord = isChecked;
            }
        });
        findViewById(R.id.btn_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (authorized) {
                    start(false);
                } else {
                    Snackbar.make(MainActivity.this.getWindow().getDecorView().getRootView(), "streaming need permissions!", Snackbar.LENGTH_LONG)
                            .setAction("auth", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    verifyPermissions();
                                }
                            }).show();
                }
            }
        });
        findViewById(R.id.btn_record_squqre).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (authorized) {
                    start(true);
                } else {
                    Snackbar.make(MainActivity.this.getWindow().getDecorView().getRootView(), "streaming need permissions!", Snackbar.LENGTH_LONG)
                            .setAction("auth", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    verifyPermissions();
                                }
                            }).show();
                }
            }
        });
        verifyPermissions();
    }

    private void start(boolean isSquare) {
        Intent intent;
        if (continuedRecord) {
            intent = new Intent(MainActivity.this, RecordingActivity2.class);
        } else {
            intent = new Intent(MainActivity.this, RecordingActivity.class);
        }

        intent.putExtra(RecordingActivity.IS_SQUARE, isSquare);
        startActivity(intent);
    }

    public void verifyPermissions() {
        int CAMERA_permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        int RECORD_AUDIO_permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO);
        int WRITE_EXTERNAL_STORAGE_permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (CAMERA_permission != PackageManager.PERMISSION_GRANTED ||
                RECORD_AUDIO_permission != PackageManager.PERMISSION_GRANTED ||
                WRITE_EXTERNAL_STORAGE_permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STREAM,
                    REQUEST_STREAM
            );
            authorized = false;
        } else {
            authorized = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STREAM) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                authorized = true;
            }
        }
    }
}