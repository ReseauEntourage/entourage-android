package social.entourage.android.message.push;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;

import java.io.IOException;

/**
 * Created by NTE on 31/07/15.
 */
public class RegisterGCMService extends IntentService {

    private final String GCM_SENDER_ID = "1085027645289"; // to be stored int the shared preferences
    public static final String SHARED_PREFERENCES_FILE = "ENTOURAGE_GCM_DATA";
    private static final String KEY_APPLICATION_VERSION = "ENTOURAGE_APPLICATION_VERSION";
    public static final String KEY_REGISTRATION_ID = "ENTOURAGE_REGISTRATION_ID";
    private static final int ENTOURAGE_MIN_VERSION = 0;

    private final SpiceManager spiceManager;

    public RegisterGCMService() {
        super("RegisterGCMService");
        spiceManager = new SpiceManager(UncachedSpiceService.class);
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        spiceManager.start(this);
    }

    @Override
    public void onDestroy() {
        if (spiceManager.isStarted()) {
            spiceManager.shouldStop();
        }
        super.onDestroy();
    }

    // ----------------------------------
    // GCM METHODS
    // ----------------------------------

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);

        int storedVersion = sharedPreferences.getInt(KEY_APPLICATION_VERSION, ENTOURAGE_MIN_VERSION);
        int currentVersion = getCurrentCodeVersion();

        if (storedVersion < currentVersion) {

            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            String registrationId = null;
            try {
                registrationId = gcm.register(GCM_SENDER_ID);
            } catch (IOException e) {
                Log.e("Error", "Can not register Google Cloud Messaging");
            }

            if (registrationId != null) {
                sharedPreferences.edit().putString(KEY_REGISTRATION_ID, registrationId);
            }

        }

    }

    private int getCurrentCodeVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e("Error", "Can not get package info");
        }
        return -1;
    }
}
