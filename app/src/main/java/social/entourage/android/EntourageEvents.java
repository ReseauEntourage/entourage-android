package social.entourage.android;

import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryAgentListener;

/**
 * Wrapper for sending events to different aggregators
 * Created by Mihai Ionescu on 03/10/2017.
 */

public class EntourageEvents implements FlurryAgentListener {

    private static String TAG = EntourageEvents.class.getSimpleName();
    static boolean isFlurrySessionStarted = false;

    @Override
    public void onSessionStarted() {
        isFlurrySessionStarted=true;
    }

    public static void logEvent(String event) {
        if(isFlurrySessionStarted) {
            FlurryAgent.logEvent(event);
        } else {
            Log.w(TAG, "Trying to send to Flurry without an opened session: "+event);
        }
        if(EntourageApplication.get().getMixpanel()!= null) {
            EntourageApplication.get().getMixpanel().track(event, null);
        }
    }

}
