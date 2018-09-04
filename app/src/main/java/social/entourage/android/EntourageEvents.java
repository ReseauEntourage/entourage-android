package social.entourage.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.PermissionChecker;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.Locale;

import social.entourage.android.api.model.User;
import social.entourage.android.message.push.RegisterGCMService;

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

        String geolocStatus = isPermissionGranted? "YES":"NO";
        people.set("EntourageGeolocEnable", geolocStatus);
        if(EntourageApplication.get().getFirebase()!=null) {
            EntourageApplication.get().getFirebase().setUserProperty("EntourageGeolocEnable", geolocStatus);
        }
    }

    public static void updateMixpanelInfo(User user, Context context, boolean areNotificationsEnabled) {
        FirebaseAnalytics mFirebaseAnalytics = EntourageApplication.get().getFirebase();
        MixpanelAPI mixpanel = EntourageApplication.get().getMixpanel();

        mixpanel.identify(String.valueOf(user.getId()));
        MixpanelAPI.People people = mixpanel.getPeople();
        people.identify(String.valueOf(user.getId()));
        mFirebaseAnalytics.setUserId(String.valueOf(user.getId()));

        Crashlytics.setUserIdentifier(String.valueOf(user.getId()));
        Crashlytics.setUserEmail(user.getEmail());

        people.set("$email", user.getEmail());
        people.set("EntourageUserType", user.isPro()?"Pro":"Public");
        people.set("Language", Locale.getDefault().getLanguage());

        mFirebaseAnalytics.setUserProperty("Email", user.getEmail());
        mFirebaseAnalytics.setUserProperty("EntourageUserType", user.isPro()?"Pro":"Public");
        mFirebaseAnalytics.setUserProperty("Language", Locale.getDefault().getLanguage());

        if(user.getPartner()!=null) {
            people.set("EntouragePartner", user.getPartner().getName());
            mFirebaseAnalytics.setUserProperty("EntouragePartner", user.getPartner().getName());
        }

        String geolocStatus="NO";
        if (PermissionChecker.checkSelfPermission(context, user.getLocationAccessString()) == PackageManager.PERMISSION_GRANTED) {
            geolocStatus = "YES";
        }
        people.set("EntourageGeolocEnable", geolocStatus);
        mFirebaseAnalytics.setUserProperty("EntourageGeolocEnable", geolocStatus);

        final SharedPreferences sharedPreferences = EntourageApplication.get().getSharedPreferences();
        boolean notificationsEnabled = sharedPreferences.getBoolean(EntourageApplication.KEY_NOTIFICATIONS_ENABLED, false);
        people.set("EntourageNotifEnable", notificationsEnabled && areNotificationsEnabled ?"YES":"NO");
        mFirebaseAnalytics.setUserProperty("EntourageNotifEnable", notificationsEnabled && areNotificationsEnabled ?"YES":"NO");

        if(notificationsEnabled) {
            people.setPushRegistrationId(sharedPreferences.getString(EntourageApplication.KEY_REGISTRATION_ID, null));
        }
    }
}
