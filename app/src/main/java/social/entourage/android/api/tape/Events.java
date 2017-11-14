package social.entourage.android.api.tape;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.Poi;

public class Events {

    /**
     * Event triggering the checking of the intent action
     */
    public static class OnCheckIntentActionEvent {

        public OnCheckIntentActionEvent() {}

    }

    /**
     * Event bearing connection state for the offline encounters queue
     */
    public static class OnConnectionChangedEvent {

        private boolean connected;

        public OnConnectionChangedEvent(boolean connected) {
            this.connected = connected;
        }

        public boolean isConnected() {
            return connected;
        }
    }

    /**
     * Event bearing the registration id when obtained from Google Cloud Messaging (for push notifications)
     */
    public static class OnGCMTokenObtainedEvent {

        private String registrationId;

        public OnGCMTokenObtainedEvent(String registrationId) {
            this.registrationId = registrationId;
        }

        public String getRegistrationId() {
            return registrationId;
        }
    }

    /**
     * Event bearing the new location on which to center the map
     */
    public static class OnBetterLocationEvent {

        private LatLng location;

        public OnBetterLocationEvent(LatLng location) {
            this.location = location;
        }

        public LatLng getLocation() {
            return location;
        }
    }

    /**
     * Event bearing the user's choice regarding the displaying of his past tours
     */
    public static class OnUserChoiceEvent {

        private boolean userHistory;

        public OnUserChoiceEvent(boolean userHistory) {
            this.userHistory = userHistory;
        }

        public boolean isUserHistory() {
            return userHistory;
        }
    }

    /**
     * Event signaling that current user is unauthorized
     */
    public static class OnUnauthorizedEvent {

        public OnUnauthorizedEvent() {}

    }

    /**
     * Event signaling that user info is updated
     */
    public static class OnUserInfoUpdatedEvent {

        public OnUserInfoUpdatedEvent() {}
    }

    /**
     * Event signaling that user wants to act on a tour
     */
    public static class OnUserActEvent {

        public static String ACT_JOIN = "join";
        public static String ACT_QUIT = "quit";

        private String act;
        private FeedItem feedItem;

        public OnUserActEvent(String act, FeedItem feedItem) {
            this.act = act;
            this.feedItem = feedItem;
        }

        public String getAct() {
            return act;
        }

        public FeedItem getFeedItem() {
            return feedItem;
        }
    }

    /**
     * Event signaling that the user wants to update a tour join request
     */
    public static class OnUserJoinRequestUpdateEvent {

        private int userId;
        private String update;
        private FeedItem feedItem;

        public OnUserJoinRequestUpdateEvent(int userId, String update, FeedItem feedItem) {
            this.userId = userId;
            this.update = update;
            this.feedItem = feedItem;
        }

        public int getUserId() {
            return userId;
        }

        public String getUpdate() {
            return update;
        }

        public FeedItem getFeedItem() {
            return feedItem;
        }
    }

    /**
     * Event signaling that user view is requested
     */
    public static class OnUserViewRequestedEvent {

        private int userId;

        public OnUserViewRequestedEvent(int userId) {
            this.userId = userId;
        }

        public int getUserId() {
            return userId;
        }
    }

    /**
     * Event signaling that tour info view is requested
     */
    public static class OnFeedItemInfoViewRequestedEvent {

        private FeedItem feedItem;
        private int feedItemType = 0;
        private long feedItemId = 0;
        private String feedItemShareURL;
        private long invitationId = 0;
        private int feedRank = 0;

        public OnFeedItemInfoViewRequestedEvent(FeedItem feedItem) {
            this.feedItem = feedItem;
        }

        public OnFeedItemInfoViewRequestedEvent(FeedItem feedItem, int feedRank) {
            this.feedItem = feedItem;
            this.feedRank = feedRank;
        }

        public OnFeedItemInfoViewRequestedEvent(int feedItemType, long feedItemId) {
            this.feedItemType = feedItemType;
            this.feedItemId = feedItemId;
        }

        public OnFeedItemInfoViewRequestedEvent(int feedItemType, String feedItemShareURL) {
            this.feedItemType = feedItemType;
            this.feedItemShareURL = feedItemShareURL;
        }

        public OnFeedItemInfoViewRequestedEvent(int feedItemType, long feedItemId, long invitationId) {
            this.feedItemType = feedItemType;
            this.feedItemId = feedItemId;
            this.invitationId = invitationId;
        }

        public FeedItem getFeedItem() {
            return feedItem;
        }

        public long getFeedItemId() {
            return feedItemId;
        }

        public int getFeedItemType() {
            return feedItemType;
        }

        public long getInvitationId() {
            return invitationId;
        }

        public int getfeedRank() {
            return feedRank;
        }

        public String getFeedItemShareURL() {
            return feedItemShareURL;
        }
    }

    /**
     * Event signaling that tour info view is requested
     */
    public static class OnTourEncounterViewRequestedEvent {

        private Encounter encounter;

        public OnTourEncounterViewRequestedEvent(Encounter encounter) {
            this.encounter = encounter;
        }

        public Encounter getEncounter() {
            return encounter;
        }
    }

    /**
     * Event signaling that the tour needs to be closed/freezed
     */
    public static class OnFeedItemCloseRequestEvent {

        private FeedItem feedItem;
        private boolean showUI = true;

        public OnFeedItemCloseRequestEvent(FeedItem feedItem) {
            this.feedItem = feedItem;
        }

        public OnFeedItemCloseRequestEvent(FeedItem feedItem, boolean showUI) {
            this.feedItem = feedItem;
            this.showUI = showUI;
        }

        public FeedItem getFeedItem() {
            return feedItem;
        }

        public boolean isShowUI() {
            return showUI;
        }
    }

    /**
     * Event triggering the tours service location listener when the permission has been granted
     */
    public static class OnLocationPermissionGranted {

        private boolean isPermissionGranted;

        public OnLocationPermissionGranted(boolean isPermissionGranted) {
            this.isPermissionGranted = isPermissionGranted;
        }

        public boolean isPermissionGranted() {
            return isPermissionGranted;
        }
    }

    /**
     * Event signaling a push notification has been received
     */
    public static class OnPushNotificationReceived {

        private Message message;

        public OnPushNotificationReceived(Message message) {
            this.message = message;
        }

        public Message getMessage() {
            return message;
        }
    }

    /**
     * Event signaling that an encounter was created
     */
    public static class OnEncounterCreated {

        private Encounter encounter;

        public OnEncounterCreated(Encounter encounter) {
            this.encounter = encounter;
        }

        public Encounter getEncounter() {
            return encounter;
        }

    }

    /**
     * Event signaling that an encounter was updated
     */
    public static class OnEncounterUpdated {

        private Encounter encounter;

        public OnEncounterUpdated(Encounter encounter) {
            this.encounter = encounter;
        }

        public Encounter getEncounter() {
            return encounter;
        }

    }

    /**
     * Event signaling that an entourage was created
     */
    public static class OnEntourageCreated {

        private Entourage entourage;

        public OnEntourageCreated(Entourage entourage) {
            this.entourage = entourage;
        }

        public Entourage getEntourage() {
            return entourage;
        }

    }

    /**
     * Event signaling that an entourage was updated
     */
    public static class OnEntourageUpdated {

        private Entourage entourage;

        public OnEntourageUpdated(Entourage entourage) {
            this.entourage = entourage;
        }

        public Entourage getEntourage() {
            return entourage;
        }

    }

    /**
     * Event signaling that the map filter was changed
     */
    public static class OnMapFilterChanged {

        public OnMapFilterChanged() {
        }

    }

    /**
     * Event signaling that the solidarity guide filter was changed
     */
    public static class OnSolidarityGuideFilterChanged {

        public OnSolidarityGuideFilterChanged() {
        }

    }

    /**
     * Event signaling that the map filter was changed
     */
    public static class OnMyEntouragesFilterChanged {

        public OnMyEntouragesFilterChanged() {
        }

    }

    /**
     * Event signaling that a photo was taken/chosen
     */
    public static class OnPhotoChosen {

        private Uri photoUri;

        public OnPhotoChosen(Uri photoUri) {
            this.photoUri = photoUri;
        }

        public Uri getPhotoUri() {
            return photoUri;
        }
    }

    /**
     * Event signaling that an invitation status was changed
     */

    public static class OnInvitationStatusChanged {

        private FeedItem feedItem;
        private String status;

        public OnInvitationStatusChanged(FeedItem feedItem, String status) {
            this.feedItem = feedItem;
            this.status = status;
        }

        public FeedItem getFeedItem() {
            return feedItem;
        }

        public String getStatus() {
            return status;
        }
    }

    /**
     * Event signaling that partner view is requested
     */

    public static class OnPartnerViewRequestedEvent {

        private long partnerId;

        public OnPartnerViewRequestedEvent(long partnerId) {
            this.partnerId = partnerId;
        }

        public long getPartnerId() {
            return partnerId;
        }
    }

    /**
     * Event signaling that poi view is requested
     */

    public static class OnPoiViewRequestedEvent {

        private Poi poi;

        public OnPoiViewRequestedEvent(Poi poi) {
            this.poi = poi;
        }

        public Poi getPoi() {
            return poi;
        }
    }

    /**
     * Event signaling that loading more newsfeed is requested
     */

    public static class OnNewsfeedLoadMoreEvent {

        public OnNewsfeedLoadMoreEvent() {
        }

    }

}
