package social.entourage.android;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;

import static social.entourage.android.BuildConfig.FLAVOR;

/**
 * Created by Mihai Ionescu on 27/04/2018.
 */
public abstract class BaseLibrariesSupport {

    // ----------------------------------
    // Members
    // ----------------------------------

    private MixpanelAPI mixpanel;
    private FirebaseAnalytics mFirebaseAnalytics;

    // ----------------------------------
    // Public methods
    // ----------------------------------

    public void setupLibraries(Context context) {
        setupFabric(context);
        setupMixpanel(context);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void onActivityDestroyed(EntourageActivity activity) {
        if (mixpanel != null) {
            mixpanel.flush();
        }
    }

    // ----------------------------------
    // Libraries setup
    // ----------------------------------

    private void setupFabric(Context context) {
        Fabric.with(context, new Crashlytics());
        Fabric.with(context, new Answers());
    }

    private void setupMixpanel(Context context) {
        mixpanel = MixpanelAPI.getInstance(context, BuildConfig.MIXPANEL_TOKEN);
        JSONObject props = new JSONObject();
        try {
            props.put("Flavor", FLAVOR);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.registerSuperProperties(props);
    }

    // ----------------------------------
    // Push notifications and badge handling
    // ----------------------------------

    public MixpanelAPI getMixpanel() {
        return mixpanel;
    }

    public FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }
}
