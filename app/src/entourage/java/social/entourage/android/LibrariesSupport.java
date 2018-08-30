package social.entourage.android;

import android.content.Context;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;

/**
 * Libraries support class specific to Entourage
 * Added: Facebook SDK
 * Created by Mihai Ionescu on 27/04/2018.
 */
public class LibrariesSupport extends BaseLibrariesSupport {

    @Override
    public void setupLibraries(final Context context) {
        super.setupLibraries(context);
        setupFacebookSDK();
    }

    // ----------------------------------
    // Libraries setup
    // ----------------------------------

    private void setupFacebookSDK() {
        if (BuildConfig.DEBUG) {
            Log.d("Facebook", "version " + FacebookSdk.getSdkVersion());
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
        }
    }
}
