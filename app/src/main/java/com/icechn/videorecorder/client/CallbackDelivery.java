package com.icechn.videorecorder.client;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * Created by lake on 16-4-11.
 */
public class CallbackDelivery {
    static private CallbackDelivery instance;
    private final Executor mCallbackPoster;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static CallbackDelivery i() {
        return instance == null ? instance = new CallbackDelivery() : instance;
    }

    private CallbackDelivery() {
        mCallbackPoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }

    public void post(Runnable runnable) {
        mCallbackPoster.execute(runnable);
    }
}
