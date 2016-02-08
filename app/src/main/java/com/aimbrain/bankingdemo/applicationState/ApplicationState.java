package com.aimbrain.bankingdemo.applicationState;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.aimbrain.bankingdemo.LauncherActivity;
import com.aimbrain.bankingdemo.MainActivity;
import com.aimbrain.bankingdemo.SignInActivity;


public class ApplicationState implements Application.ActivityLifecycleCallbacks {

    public static final long CHECK_DELAY = 500;
    public static final String TAG = "TESTING";
    private static ApplicationState instance;

    private boolean foreground = true, paused = false;
    private Handler handler = new Handler();
    private Runnable check;


    public static void initialize(Application application) {
        if (instance == null) {
            instance = new ApplicationState();
            application.registerActivityLifecycleCallbacks(instance);
        }
    }

    public static ApplicationState getInstance(){
        return instance;
    }

    public boolean isForeground() {
        return foreground;
    }

    public boolean isBackground() {
        return !foreground;
    }

    public boolean isInitializedAgain(Activity activity){
        return (activity.getClass() == LauncherActivity.class) || (activity.getClass() == MainActivity.class);
    }

    @Override
    public void onActivityResumed(Activity activity) {
//        Log.i(TAG, "resumed" + activity.toString());
        paused = false;
        foreground = true;

        if (check != null)
            handler.removeCallbacks(check);
    }

    @Override
    public void onActivityPaused(Activity activity) {
//        Log.i(TAG, "paused " + activity.toString() );
        paused = true;

        if (check != null)
            handler.removeCallbacks(check);

        handler.postDelayed(check = new Runnable() {
            @Override
            public void run() {
                if (foreground && paused) {
                    foreground = false;
                    Log.i(TAG, "went background");
                } else {
                    Log.i(TAG, "still foreground");
                }
            }
        }, CHECK_DELAY);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if(isBackground() && !isInitializedAgain(activity)) {
            Intent intent = new Intent(activity, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        }

//        Log.i(TAG, "started " + activity.toString());
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//        Log.i(TAG, "created " + activity.toString());
    }

    @Override
    public void onActivityStopped(Activity activity) {
//        Log.i(TAG, "stopped "+ activity.toString());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}