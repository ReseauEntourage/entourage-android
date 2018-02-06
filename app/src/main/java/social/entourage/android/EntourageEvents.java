package social.entourage.android;

import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Wrapper for sending events to different aggregators
 * Created by Mihai Ionescu on 03/10/2017.
 */

public class EntourageEvents {

    private static String TAG = EntourageEvents.class.getSimpleName();

    public static void logEvent(String event) {
        if(EntourageApplication.get().getMixpanel()!= null) {
            EntourageApplication.get().getMixpanel().track(event, null);
        }
        if(EntourageApplication.get().getFirebase()!= null) {
            EntourageApplication.get().getFirebase().logEvent(event, null);
        }
    }

}
