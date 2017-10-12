package social.entourage.android;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.flurry.android.FlurryAgent;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import net.danlew.android.joda.JodaTimeAndroid;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import me.leolin.shortcutbadger.ShortcutBadger;
import social.entourage.android.api.ApiModule;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.AuthenticationModule;
import social.entourage.android.authentication.ComplexPreferences;
import social.entourage.android.authentication.login.LoginActivity;
import social.entourage.android.newsfeed.FeedItemsStorage;

import static social.entourage.android.BuildConfig.BUILD_TYPE;
import static social.entourage.android.BuildConfig.FLAVOR;
import static social.entourage.android.BuildConfig.MIXPANEL_TOKEN;

/**
 * Application setup for Flurry, JodaTime and Dagger
 */
public class EntourageApplication extends Application {

    private static EntourageApplication instance;
    public static EntourageApplication get() { return instance; }

    private EntourageComponent component;

    private ArrayList<EntourageActivity> activities;

    public int badgeCount = 0;
    public ArrayList<Message> pushNotifications = new ArrayList<>();

    private FeedItemsStorage feedItemsStorage;

    private MixpanelAPI mixpanel;

    @Override
    public void onCreate() {
        activities = new ArrayList<>();
        super.onCreate();
        instance = this;

        setupFabric();
        setupFlurry();
        setupMixpanel();
        JodaTimeAndroid.init(this);
        setupDagger();
        setupBadgeCount();
        setupFeedItemsStorage();
    }

    private void setupFabric() {
        Fabric.with(this, new Crashlytics());
        Fabric.with(this, new Answers());
    }

    private void setupDagger() {
        component = DaggerEntourageComponent.builder()
                .entourageModule(new EntourageModule(this))
                .apiModule(new ApiModule())
                .authenticationModule(new AuthenticationModule())
                .build();
        component.inject(this);
    }

    private void setupFlurry() {
        FlurryAgent.setLogEnabled(true);
        FlurryAgent.setLogLevel(Log.VERBOSE);
        FlurryAgent.setLogEvents(true);
        FlurryAgent.init(this, BuildConfig.FLURRY_API_KEY);
    }

    private void setupMixpanel() {
        mixpanel = MixpanelAPI.getInstance(this, BuildConfig.MIXPANEL_TOKEN);
        JSONObject props = new JSONObject();
        try {
            props.put("Flavor", FLAVOR);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.registerSuperProperties(props);
    }

    private void setupBadgeCount() {
        decreaseBadgeCount(0);
    }

    public EntourageComponent getEntourageComponent() {
        return component;
    }

    public MixpanelAPI getMixpanel() {
        return mixpanel;
    }

    public static EntourageApplication get(Context context) {
        return (EntourageApplication) context.getApplicationContext();
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
        if (mixpanel != null) {
            mixpanel.flush();
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
            Log.d(null, "Finishing login activity");
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
        pushNotifications.add(message);
        updateFeedItemsStorage(message, true);
        badgeCount++;
        ShortcutBadger.applyCount(this, badgeCount);
    }

    public void removePushNotificationsForFeedItem(FeedItem feedItem) {
        long feedItemId = feedItem.getId();
        int feedType = feedItem.getType();
        int count = 0;
        for (int i = 0; i < pushNotifications.size(); i++) {
            Message message = pushNotifications.get(i);
            if (message == null) {
                continue;
            }
            PushNotificationContent content = message.getContent();
            if (content != null && content.getJoinableId() == feedItemId) {
                if (FeedItem.TOUR_CARD == feedType && content.isTourRelated()) {
                    if (PushNotificationContent.TYPE_NEW_JOIN_REQUEST.equals(content.getType())) {
                        // Don't delete the join requests push, just hide them
                        message.setVisible(false);
                    }
                    else {
                        pushNotifications.remove(i);
                        i--;
                    }
                    count++;
                    continue;
                }
                if (FeedItem.ENTOURAGE_CARD == feedType && content.isEntourageRelated()) {
                    if (PushNotificationContent.TYPE_NEW_JOIN_REQUEST.equals(content.getType())) {
                        // Don't delete the join requests push, just hide them
                        message.setVisible(false);
                    }
                    else {
                        pushNotifications.remove(i);
                        i--;
                    }
                    count++;
                }
            }
        }
        feedItem.setBadgeCount(0);
        updateFeedItemsStorage(feedItem);

        decreaseBadgeCount(count);
    }

    public void removePushNotification(Message message) {
        if (message == null) {
            return;
        }
        for (Message msg : pushNotifications) {
            if (msg.getPushNotificationId() == message.getPushNotificationId()) {
                if (pushNotifications.remove(msg)) {
                    updateFeedItemsStorage(message, false);
                    decreaseBadgeCount(1);
                    break;
                }
            }
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

        int count = 0;
        // search for a push notification that matches our parameters
        for (int i = 0; i < pushNotifications.size(); i++) {
            Message message = pushNotifications.get(i);
            if (message == null) {
                continue;
            }
            PushNotificationContent content = message.getContent();
            if (content != null && content.getJoinableId() == feedId && content.getType() != null && content.getType().equals(pushType)) {
                if (FeedItem.TOUR_CARD == feedType && content.isTourRelated()) {
                    // remove the notification from the system
                    notificationManager.cancel(message.getPushNotificationId());
                    // remove the notification from our internal list
                    pushNotifications.remove(i);
                    if (message.isVisible()) {
                        updateFeedItemsStorage(message, false);
                        count++;
                    }
                    break;
                }
                if (FeedItem.ENTOURAGE_CARD == feedType && content.isEntourageRelated()) {
                    notificationManager.cancel(message.getPushNotificationId());
                    pushNotifications.remove(i);
                    if (message.isVisible()) {
                        updateFeedItemsStorage(message, false);
                        count++;
                    }
                    break;
                }
            }
        }
        decreaseBadgeCount(count);
    }

    public void removeAllPushNotifications() {
        // get the notification manager
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        // cancel all the notifications
        for (Message msg : pushNotifications) {
            notificationManager.cancel(msg.getPushNotificationId());
        }
        // remove all the notifications from our internal list
        pushNotifications.clear();
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

}
