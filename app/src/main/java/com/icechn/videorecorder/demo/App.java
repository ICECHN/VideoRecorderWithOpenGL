package com.icechn.videorecorder.demo;

import android.app.Application;

/**
 * Created by ICE on 2017/10/12.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        _app = this;
    }
    public static Application getApp() {
        return _app;
    }
    private static Application _app;
}
