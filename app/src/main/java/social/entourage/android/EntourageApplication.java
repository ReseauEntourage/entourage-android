package social.entourage.android;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.flurry.android.FlurryAgent;

import net.danlew.android.joda.JodaTimeAndroid;

import social.entourage.android.api.ApiModule;
import social.entourage.android.authentication.AuthenticationModule;

public class EntourageApplication extends Application {

    private EntourageComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        setupFlurry();
        JodaTimeAndroid.init(this);
        setupDagger();
    }

    private void setupDagger() {
        component = DaggerEntourageComponent.builder()
                .entourageModule(new EntourageModule(this))
                .apiModule(new ApiModule())
                .authenticationModule(new AuthenticationModule())
                .build();
        component.inject(this);
    }

    private void setupFlurry() {
        FlurryAgent.setLogEnabled(true);
        FlurryAgent.setLogLevel(Log.VERBOSE);
        //FlurryAgent.setReportLocation(true);
        FlurryAgent.setLogEvents(true);
        FlurryAgent.init(this, BuildConfig.FLURRY_API_KEY);
    }

    public EntourageComponent getEntourageComponent() {
        return component;
    }

    public static EntourageApplication get(Context context) {
        return (EntourageApplication) context.getApplicationContext();
    }
}
