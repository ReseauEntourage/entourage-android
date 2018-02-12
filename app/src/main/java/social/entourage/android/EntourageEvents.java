package social.entourage.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.PermissionChecker;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.Locale;

import social.entourage.android.api.model.User;
import social.entourage.android.message.push.RegisterGCMService;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

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

    public static void onLocationPermissionGranted(boolean isPermissionGranted) {
        MixpanelAPI mixpanel = EntourageApplication.get().getMixpanel();
        if (mixpanel == null) return;
        MixpanelAPI.People people = mixpanel.getPeople();
        if (people == null) return;

        people.set("EntourageGeolocEnable", isPermissionGranted ? "YES" : "NO");
    }

    public static void updateMixpanelInfo(User user, Context context, boolean areNotificationsEnabled) {
        MixpanelAPI mixpanel = EntourageApplication.get().getMixpanel();
        mixpanel.identify(String.valueOf(user.getId()));
        MixpanelAPI.People people = mixpanel.getPeople();
        people.identify(String.valueOf(user.getId()));
        FirebaseAnalytics mFirebaseAnalytics = EntourageApplication.get().getFirebase();

        people.set("$email", user.getEmail());
        people.set("EntouragePartner", user.getPartner());
        people.set("EntourageUserType", user.isPro()?"Pro":"Public");
        people.set("Language", Locale.getDefault().getLanguage());

        mFirebaseAnalytics.setUserProperty("$email", user.getEmail());
        mFirebaseAnalytics.setUserProperty("EntouragePartner", user.getPartner().getName());
        mFirebaseAnalytics.setUserProperty("EntourageUserType", user.isPro()?"Pro":"Public");
        mFirebaseAnalytics.setUserProperty("Language", Locale.getDefault().getLanguage());

        if (PermissionChecker.checkSelfPermission(context, user.isPro() ? ACCESS_FINE_LOCATION : ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            people.set("EntourageGeolocEnable", "YES");
            mFirebaseAnalytics.setUserProperty("EntourageGeolocEnable", "YES");
        } else {
            people.set("EntourageGeolocEnable", "NO");
            mFirebaseAnalytics.setUserProperty("EntourageGeolocEnable", "NO");
        }

        final SharedPreferences sharedPreferences = context.getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE_GCM, Context.MODE_PRIVATE);
        boolean notificationsEnabled = sharedPreferences.getBoolean(RegisterGCMService.KEY_NOTIFICATIONS_ENABLED, false);
        people.set("EntourageNotifEnable", notificationsEnabled && areNotificationsEnabled ?"YES":"NO");
        mFirebaseAnalytics.setUserProperty("EntourageNotifEnable", notificationsEnabled && areNotificationsEnabled ?"YES":"NO");

        if(notificationsEnabled) {
            people.setPushRegistrationId(sharedPreferences.getString(RegisterGCMService.KEY_REGISTRATION_ID, null));
        }
    }


}
