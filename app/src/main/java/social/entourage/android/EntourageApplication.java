package social.entourage.android;

import android.app.Application;
import android.content.Context;

import com.flurry.android.FlurryAgent;

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
        FlurryAgent.setReportLocation(true);
        FlurryAgent.setLogEvents(true);
        FlurryAgent.setLogEnabled(true);
        FlurryAgent.init(this, BuildConfig.FLURRY_API_KEY);
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
