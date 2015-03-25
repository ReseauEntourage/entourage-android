package com.octo.entourage;

import android.app.Application;
import android.content.Context;

import com.octo.appaloosasdk.Appaloosa;

import net.danlew.android.joda.JodaTimeAndroid;

import dagger.ObjectGraph;

public class EntourageApplication extends Application {

    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);

        objectGraph = ObjectGraph.create(Modules.list(this));
        inject(this);

        if (BuildConfig.APPALOOSA_AUTO_UPDATE) {
            Appaloosa.getInstance().autoUpdate(
                    this,
                    BuildConfig.APPALOOSA_STORE_ID,
                    BuildConfig.APPALOOSA_STORE_TOKEN);
        }
    }

    public void inject(final Object o) {
        objectGraph.inject(o);
    }

    public ObjectGraph getApplicationGraph() {
        return objectGraph;
    }

    public static EntourageApplication get(Context context) {
        return (EntourageApplication) context.getApplicationContext();
    }
}
