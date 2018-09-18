package com.mualab.org.user.application;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;

/**
 * Created by mindiii on 13/6/18.
 */

public class AppLifeCycle implements Application.ActivityLifecycleCallbacks , ComponentCallbacks2 {

    private boolean appInForeground = false;
    LifeCycleDelegateListner lifeCycleDelegate;


    public AppLifeCycle(LifeCycleDelegateListner lifeCycleDelegate) {
        this.lifeCycleDelegate = lifeCycleDelegate;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (!appInForeground) {
            appInForeground = true;
            lifeCycleDelegate.onAppForegrounded();
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (!appInForeground) {
            appInForeground = true;
            lifeCycleDelegate.onAppForegrounded();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @Override
    public void onTrimMemory(int level) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            appInForeground = false;
            lifeCycleDelegate.onAppBackgrounded();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {

    }

    @Override
    public void onLowMemory() {

    }
}

interface LifeCycleDelegateListner {
    void onAppBackgrounded();
    void onAppForegrounded();
}