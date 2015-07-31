package social.entourage.android;

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

    private final String GCM_SENDER_ID = "1085027645289";

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

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("shared_pref_file", Context.MODE_PRIVATE);

        int storedVersion = sharedPreferences.getInt("key_app_version", 0);
        int currentVersion = getCurrentCodeVersion();

        //if (storedVersion < currentVersion) {     A REMETTRE

            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            String registrationId = null;
            try {
                registrationId = gcm.register(GCM_SENDER_ID); //  à mettre à jour
            } catch (IOException e) {
                // do nothing
            }

            if (registrationId != null) {
                Log.v("reg id", registrationId);
            }

        //}

    }

    private int getCurrentCodeVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e) {
            // do nothing
        }
        return -1;
    }
}
