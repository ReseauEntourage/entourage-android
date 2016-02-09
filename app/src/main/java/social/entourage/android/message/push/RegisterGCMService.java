package social.entourage.android.message.push;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import social.entourage.android.api.tape.Events.*;
import social.entourage.android.tools.BusProvider;

/**
 * Service providing the registration to Google Cloud Messaging
 */
public class RegisterGCMService extends IntentService {

    private final String GCM_SENDER_ID = "1085027645289"; // to be stored int the shared preferences ?
    private final String GCM_SCOPE = "GCM";
    public static final String SHARED_PREFERENCES_FILE_GCM = "ENTOURAGE_GCM_DATA";
    private static final String KEY_APPLICATION_VERSION = "ENTOURAGE_APPLICATION_VERSION";
    public static final String KEY_REGISTRATION_ID = "ENTOURAGE_REGISTRATION_ID";
    private static final int ENTOURAGE_MIN_VERSION = 0;

    public RegisterGCMService() {
        super("RegisterGCMService");
    }

    // ----------------------------------
    // GCM METHODS
    // ----------------------------------

    @Override
    protected void onHandleIntent(Intent intent) {

        final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_FILE_GCM, Context.MODE_PRIVATE);

        int storedVersion = sharedPreferences.getInt(KEY_APPLICATION_VERSION, ENTOURAGE_MIN_VERSION);
        int currentVersion = getCurrentCodeVersion();

        if (storedVersion < currentVersion) {

            String registrationId = null;
            try {
                registrationId = InstanceID.getInstance(this).getToken(GCM_SENDER_ID, GCM_SCOPE);
            } catch (IOException e) {
                Log.e("Error", "Can not register Google Cloud Messaging");
            }

            if (registrationId != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_REGISTRATION_ID, registrationId);
                editor.commit();
                BusProvider.getInstance().register(this);
                BusProvider.getInstance().post(new GCMTokenObtainedEvent(registrationId));
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
