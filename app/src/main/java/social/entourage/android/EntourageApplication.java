package social.entourage.android;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.analytics.FirebaseAnalytics;

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
    @NonNull public static EntourageApplication get() { return instance; }

    private EntourageComponent component;

    private ArrayList<EntourageActivity> activities;

    private int badgeCount = 0;

    private FeedItemsStorage feedItemsStorage;

    private SharedPreferences sharedPreferences;

    private LibrariesSupport librariesSupport;

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
        setupFeedItemsStorage();
        setupSharedPreferences();
        setupBadgeCount();
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
        updateBadgeCount();
    }

    public EntourageComponent getEntourageComponent() {
        return component;
    }

    public FirebaseAnalytics getFirebase() {
        return librariesSupport.getFirebaseAnalytics();
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public int getBadgeCount() {
        return badgeCount;
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

    private void updateBadgeCount() {
        if(me()==null || badgeCount == feedItemsStorage.getBadgeCount(me().getId())) {
            return;
        }
        badgeCount = feedItemsStorage.getBadgeCount(me().getId());
        if (badgeCount == 0) {
            ShortcutBadger.removeCount(getApplicationContext());
        } else {
            ShortcutBadger.applyCount(getApplicationContext(), badgeCount);
        }
    }

    public void addPushNotification(Message message) {
        PushNotificationManager.getInstance().addPushNotification(message);
        if(storeNewPushNotification(message, true)>1) {
            //feedItem badge was already set
            return;
        }
        updateBadgeCount();
    }

    public void removePushNotificationsForFeedItem(FeedItem feedItem) {
        int count = PushNotificationManager.getInstance().removePushNotificationsForFeedItem(feedItem);
        if(count>0) {
            updateStorageFeedItem(feedItem);
        }
    }

    public void removePushNotification(Message message) {
        if (message == null) {
            return;
        }
        int count = PushNotificationManager.getInstance().removePushNotification(message);
        if (count > 0) {
            if(storeNewPushNotification(message, false)==0) {
                //feedItem badge was set to 0
                updateBadgeCount();
            }
        }
    }

    public void removePushNotification(FeedItem feedItem, int userId, String pushType) {
        if (feedItem == null) {
            return;
        }
        removePushNotification(feedItem.getId(), feedItem.getType(), userId, pushType);
    }

    public void removePushNotification(long feedId, int feedType, int userId, String pushType) {
        int count = PushNotificationManager.getInstance().removePushNotification(feedId, feedType, userId, pushType);
        if(count>0) {
            updateBadgeCount();
        }
    }

    public void removeAllPushNotifications() {
        PushNotificationManager.getInstance().removeAllPushNotifications();
        // reset the badge count
        updateBadgeCount();
    }

    public void updateBadgeCountForFeedItem(FeedItem feedItem) {
        updateStorageFeedItem(feedItem);
        updateBadgeCount();
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

    private void storeFeedItem(FeedItem feedItem) {
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
        feedItemsStorage.saveFeedItem(me.getId(), feedItem);
    }

    public int storeNewPushNotification(Message message, boolean isAdded) {
        if (feedItemsStorage == null) {
            return -1;
        }
        if (component.getAuthenticationController() == null) {
            return -1;
        }
        User me = component.getAuthenticationController().getUser();
        if (me == null) {
            return -1;
        }
        return feedItemsStorage.saveFeedItem(me.getId(), message, isAdded);
    }

    public void updateStorageFeedItem(FeedItem feedItem) {
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
        feedItemsStorage.updateFeedItem(me.getId(), feedItem);
    }

    public boolean clearFeedStorage() {
        User me = component.getAuthenticationController().getUser();
        if (me == null) {
            return false ;
        }
        if(feedItemsStorage!=null) {
            return feedItemsStorage.clear(me.getId());
        }
        return false;
    }
}
