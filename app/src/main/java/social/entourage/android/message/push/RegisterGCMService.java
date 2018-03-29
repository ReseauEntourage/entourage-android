package social.entourage.android.message.push;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import social.entourage.android.EntourageApplication;
import social.entourage.android.api.tape.Events.*;
import social.entourage.android.tools.BusProvider;

/**
 * Service providing the registration to Google Cloud Messaging
 */
public class RegisterGCMService extends IntentService {

    public static final String GCM_SENDER_ID = "1085027645289"; // to be stored int the shared preferences ?
    private final String GCM_SCOPE = "GCM";
    public static final String SHARED_PREFERENCES_FILE_GCM = "ENTOURAGE_GCM_DATA";
    public static final String KEY_REGISTRATION_ID = "ENTOURAGE_REGISTRATION_ID";
    public static final String KEY_NOTIFICATIONS_ENABLED = "ENTOURAGE_NOTIFICATION_ENABLED";

    public RegisterGCMService() {
        super("RegisterGCMService");
    }

    // ----------------------------------
    // GCM METHODS
    // ----------------------------------

    @Override
    protected void onHandleIntent(Intent intent) {

        String registrationId = null;
        try {
            registrationId = InstanceID.getInstance(this).getToken(GCM_SENDER_ID, GCM_SCOPE);
        } catch (IOException e) {
            Log.e("Error", "Can not register Google Cloud Messaging");
        }

        if (registrationId != null) {
            final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_FILE_GCM, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_REGISTRATION_ID, registrationId);
            editor.commit();
            EntourageApplication.get().getMixpanel().getPeople().setPushRegistrationId(registrationId);
        }else if (intent!=null && intent.getStringExtra("unregistered") != null) {
            EntourageApplication.get().getMixpanel().getPeople().clearPushRegistrationId();
        }
        BusProvider.getInstance().register(this);
        BusProvider.getInstance().post(new OnGCMTokenObtainedEvent(registrationId));
    }
}
