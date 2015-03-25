package com.octo.entourage;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.octo.appaloosasdk.Appaloosa;

import java.util.List;

import dagger.ObjectGraph;

/**
 * Base activity which set up a scoped graph and inject it
 */
public abstract class EntourageActivity extends ActionBarActivity {

    private ObjectGraph activityGraph;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityGraph = EntourageApplication.get(this).getApplicationGraph().plus(getScopedModules().toArray());
        inject(this);
    }

    @Override
    protected void onDestroy() {
        activityGraph = null;
        super.onDestroy();
    }

    public void inject(Object o) {
        activityGraph.inject(o);
    }

    protected abstract List<Object> getScopedModules();
}
