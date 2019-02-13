package social.entourage.android;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.ArrayList;

import me.leolin.shortcutbadger.ShortcutBadger;
import social.entourage.android.api.ApiModule;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.AuthenticationModule;
import social.entourage.android.authentication.ComplexPreferences;
import social.entourage.android.authentication.login.LoginActivity;
import social.entourage.android.message.push.PushNotificationManager;
import social.entourage.android.newsfeed.FeedItemsStorage;
import timber.log.Timber;

/**
 * Application setup for Analytics, JodaTime and Dagger
 */
public class EntourageApplication extends MultiDexApplication {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public final static String KEY_TUTORIAL_DONE = "social.entourage.android.KEY_TUTORIAL_DONE";
    public static final String KEY_REGISTRATION_ID = "ENTOURAGE_REGISTRATION_ID";
    public static final String KEY_NOTIFICATIONS_ENABLED = "ENTOURAGE_NOTIFICATION_ENABLED";
    public static final String KEY_GEOLOCATION_ENABLED = "ENTOURAGE_GEOLOCATION_ENABLED";

    // ----------------------------------
    // MEMBERS
    // ----------------------------------

    private static EntourageApplication instance;
    public static EntourageApplication get() { return instance; }

    private EntourageComponent component;

    private ArrayList<EntourageActivity> activities;

    public int badgeCount = 0;

    private FeedItemsStorage feedItemsStorage;

    private SharedPreferences sharedPreferences;

    private LibrariesSupport librariesSupport;

    static public String ENTOURAGE_APP="entourage";
    static public String PFP_APP="pfp";

    public enum WhiteLabelApp {ENTOURAGE_APP, PFP_APP}

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public void onCreate() {
        activities = new ArrayList<>();
        super.onCreate();
        instance = this;

        librariesSupport = new LibrariesSupport();
        librariesSupport.setupLibraries(this);

        JodaTimeAndroid.init(this);
        setupDagger();
        setupBadgeCount();
        setupFeedItemsStorage();
        setupSharedPreferences();
    }



    private void setupDagger() {
        component = DaggerEntourageComponent.builder()
                .entourageModule(new EntourageModule(this))
                .apiModule(new ApiModule())
                .authenticationModule(new AuthenticationModule())
                .build();
        component.inject(this);
    }

    private void setupSharedPreferences() {
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    private void setupBadgeCount() {
        decreaseBadgeCount(0);
    }

    public EntourageComponent getEntourageComponent() {
        return component;
    }

    public MixpanelAPI getMixpanel() {
        return librariesSupport.getMixpanel();
    }

    public FirebaseAnalytics getFirebase() {
        return librariesSupport.getFirebaseAnalytics();
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static EntourageApplication get(@Nullable Context context) {
        return context!=null? (EntourageApplication) context.getApplicationContext() : EntourageApplication.get();
    }

    public static User me() {
        EntourageApplication application = EntourageApplication.get();
        if (application == null || application.component == null) return null;
        AuthenticationController authenticationController = application.component.getAuthenticationController();
        if (authenticationController == null) return null;
        return authenticationController.getUser();
    }

    public static User me(Context context) {
        if (context == null) return null;
        EntourageApplication application = EntourageApplication.get(context);
        if (application == null || application.component == null) return null;
        AuthenticationController authenticationController = application.component.getAuthenticationController();
        if (authenticationController == null) return null;
        return authenticationController.getUser();
    }

    public void onActivityCreated(EntourageActivity activity) {
        activities.add(activity);
    }

    public void onActivityDestroyed(EntourageActivity activity) {
        activities.remove(activity);
        saveFeedItemsStorage();
        if (librariesSupport != null) {
            librariesSupport.onActivityDestroyed(activity);
        }
    }

    public LoginActivity getLoginActivity() {
        for (EntourageActivity activity:activities) {
            if (activity instanceof LoginActivity) {
                return (LoginActivity)activity;
            }
        }
        return null;
    }

    public void finishLoginActivity() {
        LoginActivity loginActivity = getLoginActivity();
        if (loginActivity != null) {
            Timber.d("Finishing login activity");
            loginActivity.finish();
        }
    }

    // ----------------------------------
    // Push notifications and badge handling
    // ----------------------------------

    private void decreaseBadgeCount(int amount) {
        badgeCount -= amount;
        if (badgeCount < 0) {
            badgeCount = 0;
        }

        if (badgeCount == 0) {
            ShortcutBadger.removeCount(getApplicationContext());
        } else {
            ShortcutBadger.applyCount(getApplicationContext(), badgeCount);
        }
    }

    public void addPushNotification(Message message) {
        PushNotificationManager.getInstance().addPushNotification(message);
        updateFeedItemsStorage(message, true);
        badgeCount++;
        ShortcutBadger.applyCount(getApplicationContext(), badgeCount);
    }

    public void removePushNotificationsForFeedItem(FeedItem feedItem) {
        int count = PushNotificationManager.getInstance().removePushNotificationsForFeedItem(feedItem);
        feedItem.setBadgeCount(0);
        updateFeedItemsStorage(feedItem);

        decreaseBadgeCount(count);
    }

    public void removePushNotification(Message message) {
        if (message == null) {
            return;
        }
        int count = PushNotificationManager.getInstance().removePushNotification(message);
        if (count > 0) {
            updateFeedItemsStorage(message, false);
            decreaseBadgeCount(count);
        }
    }

    public void removePushNotification(FeedItem feedItem, int userId, String pushType) {
        if (feedItem == null) {
            return;
        }
        long feedId = feedItem.getId();
        int feedType = feedItem.getType();

        removePushNotification(feedId, feedType, userId, pushType);
    }

    public void removePushNotification(long feedId, int feedType, int userId, String pushType) {
        // Sanity checks
        if (pushType == null) {
            return;
        }

        // get the notification manager
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        int count = PushNotificationManager.getInstance().removePushNotification(feedId, feedType, userId, pushType);
        decreaseBadgeCount(count);
    }

    public void removeAllPushNotifications() {
        PushNotificationManager.getInstance().removeAllPushNotifications();
        // reset the badge count
        decreaseBadgeCount(badgeCount);
    }

    public void updateBadgeCountForFeedItem(FeedItem feedItem) {
        updateFeedItemFromStorage(feedItem);
    }

    // ----------------------------------
    // FeedItemsStorage
    // ----------------------------------

    private void setupFeedItemsStorage() {
        this.feedItemsStorage = component.getComplexPreferences().getObject(FeedItemsStorage.KEY, FeedItemsStorage.class);
        if (this.feedItemsStorage == null) {
            this.feedItemsStorage = new FeedItemsStorage();
        }
    }

    private void saveFeedItemsStorage() {
        if (component == null || feedItemsStorage == null) {
            return;
        }
        ComplexPreferences preferences = component.getComplexPreferences();
        if (preferences == null) {
            return;
        }
        preferences.putObject(FeedItemsStorage.KEY, feedItemsStorage);
        preferences.commit();
    }

    public void updateFeedItemsStorage(FeedItem feedItem) {
        if (feedItemsStorage == null) {
            return;
        }
        if (component.getAuthenticationController() == null) {
            return;
        }
        User me = component.getAuthenticationController().getUser();
        if (me == null) {
            return;
        }
        feedItemsStorage.updateFeedItemStorage(me.getId(), feedItem);
    }

    public void updateFeedItemsStorage(Message message, boolean isAdded) {
        if (feedItemsStorage == null) {
            return;
        }
        if (component.getAuthenticationController() == null) {
            return;
        }
        User me = component.getAuthenticationController().getUser();
        if (me == null) {
            return;
        }
        feedItemsStorage.updateFeedItemStorage(me.getId(), message, isAdded);
    }

    public void updateFeedItemFromStorage(FeedItem feedItem) {
        if (feedItemsStorage == null) {
            return;
        }
        if (component.getAuthenticationController() == null) {
            return;
        }
        User me = component.getAuthenticationController().getUser();
        if (me == null) {
            return;
        }
        feedItemsStorage.updateFeedItemFromStorage(me.getId(), feedItem);
    }

    // ----------------------------------
    // Multiple App support methods
    // ----------------------------------

    static boolean isCurrentApp(String appName) {
        return BuildConfig.FLAVOR.contains(appName);
    }

    public static boolean isEntourageApp() {
        return isCurrentApp(ENTOURAGE_APP);
    }

    public static boolean isPfpApp() {
        return isCurrentApp(PFP_APP);
    }
}
