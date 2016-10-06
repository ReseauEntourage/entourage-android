package social.entourage.android;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.flurry.android.FlurryAgent;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.ArrayList;

import social.entourage.android.api.ApiModule;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.AuthenticationModule;
import social.entourage.android.authentication.login.LoginActivity;

/**
 * Application setup for Flurry, JodaTime and Dagger
 */
public class EntourageApplication extends Application {

    private EntourageComponent component;

    private ArrayList<EntourageActivity> activities;

    @Override
    public void onCreate() {

        activities = new ArrayList<>();

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

    public static User me(Context context) {
        if (context == null) return null;
        EntourageApplication application = EntourageApplication.get(context);
        if (application == null || application.component == null) return null;
        AuthenticationController authenticationController = application.component.getAuthenticationController();
        if (authenticationController == null) return null;
        return authenticationController.getUser();
    }

    public void onActivityCreated(EntourageActivity activity) {
        activities.add(activity);
    }

    public void onActivityDestroyed(EntourageActivity activity) {
        activities.remove(activity);
    }

    public LoginActivity getLoginActivity() {
        for (EntourageActivity activity:activities) {
            if (activity instanceof LoginActivity) {
                return (LoginActivity)activity;
            }
        }
        return null;
    }

    public void finishLoginActivity() {
        LoginActivity loginActivity = getLoginActivity();
        if (loginActivity != null) {
            Log.d(null, "Finishing login activity");
            loginActivity.finish();
        }
    }
}
