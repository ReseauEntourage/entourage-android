package com.octo.entourage;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

public class EntourageApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);
    }
}
