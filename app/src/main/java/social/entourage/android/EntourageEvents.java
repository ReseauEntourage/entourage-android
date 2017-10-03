package social.entourage.android;

import com.flurry.android.FlurryAgent;

/**
 * Wrapper for sending events to different aggregators
 * Created by Mihai Ionescu on 03/10/2017.
 */

public class EntourageEvents {

    public static void logEvent(String event) {
        FlurryAgent.logEvent(event);
    }

}
