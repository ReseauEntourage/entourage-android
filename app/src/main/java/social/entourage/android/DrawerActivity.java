package social.entourage.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.annotation.IdRes;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.app.NotificationManagerCompat;
import androidx.appcompat.app.AlertDialog;

import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.HashSet;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.tape.Events;
import social.entourage.android.api.tape.Events.OnCheckIntentActionEvent;
import social.entourage.android.api.tape.Events.OnFeedItemCloseRequestEvent;
import social.entourage.android.api.tape.Events.OnFeedItemInfoViewRequestedEvent;
import social.entourage.android.api.tape.Events.OnGCMTokenObtainedEvent;
import social.entourage.android.api.tape.Events.OnPushNotificationReceived;
import social.entourage.android.api.tape.Events.OnTourEncounterViewRequestedEvent;
import social.entourage.android.api.tape.Events.OnUnauthorizedEvent;
import social.entourage.android.api.tape.Events.OnUserActEvent;
import social.entourage.android.api.tape.Events.OnUserViewRequestedEvent;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.UserPreferences;
import social.entourage.android.deeplinks.DeepLinksManager;
import social.entourage.android.location.LocationUtils;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.tour.choice.ChoiceFragment;
import social.entourage.android.tour.confirmation.ConfirmationFragment;
import social.entourage.android.tour.encounter.CreateEncounterActivity;
import social.entourage.android.tour.encounter.EncounterDisclaimerFragment;
import social.entourage.android.tour.encounter.ReadEncounterActivity;
import social.entourage.android.entourage.EntourageDisclaimerFragment;
import social.entourage.android.entourage.my.MyEntouragesFragment;
import social.entourage.android.tour.TourService;
import social.entourage.android.entourage.information.EntourageInformationFragment;
import social.entourage.android.message.push.PushNotificationManager;
import social.entourage.android.navigation.BottomNavigationDataSource;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.user.AvatarUploadPresenter;
import social.entourage.android.user.AvatarUploadView;
import social.entourage.android.user.UserFragment;
import social.entourage.android.user.edit.UserEditActionZoneFragment;
import social.entourage.android.user.edit.photo.PhotoChooseInterface;
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragment;
import social.entourage.android.user.edit.photo.PhotoEditFragment;
import timber.log.Timber;

public class DrawerActivity extends EntourageSecuredActivity
    implements EntourageInformationFragment.OnEntourageInformationFragmentFinish,
    ChoiceFragment.OnChoiceFragmentFinish,
    EntourageDisclaimerFragment.OnFragmentInteractionListener,
    EncounterDisclaimerFragment.OnFragmentInteractionListener,
    PhotoChooseInterface,
    UserEditActionZoneFragment.FragmentListener,
    AvatarUploadView {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    DrawerPresenter presenter;

    @Inject
    AvatarUploadPresenter avatarUploadPresenter;

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomBar;

    @BindView(R.id.content_view)
    View contentView;

    //TextView discussionBadgeView;

    private BottomNavigationDataSource navigationDataSource = new BottomNavigationDataSource();
    BadgeDrawable messageBadge;

    protected Fragment mainFragment;
    protected MapEntourageFragment mapEntourageFragment;
    private UserFragment userFragment;

    //private SharedPreferences gcmSharedPreferences;
    //private String intentAction;
    private Tour intentTour;

    @IdRes int selectedSidemenuAction;

    private boolean editActionZoneShown = false;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        ButterKnife.bind(this);

        if (isFinishing()) return;

        configureToolbar();

        if (getIntent() != null) {
            checkDeepLinks();
        }

        User user = getAuthenticationController().getUser();
        if (user != null) {
            //refresh the user info from the server
            Location location = EntourageLocation.getInstance().getCurrentLocation();
            presenter.updateUser(null, null, null, location);
            //initialize the push notifications
            initializePushNotifications();

            updateAnalyticsInfo();
        }
    }

    private void checkDeepLinks() {
        String intentAction = getIntent().getAction();
        Bundle extras = getIntent().getExtras();
        if (Intent.ACTION_VIEW.equals(intentAction)) {
            // Save the deep link intent
            DeepLinksManager.getInstance().setDeepLinkIntent(getIntent());
        } else if(extras !=null && extras.containsKey(PushNotificationManager.KEY_CTA)) {
            DeepLinksManager.getInstance().setDeepLinkIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(extras.getString(PushNotificationManager.KEY_CTA))));
        }

    }

    @Override
    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerDrawerComponent.builder()
            .entourageComponent(entourageComponent)
            .drawerModule(new DrawerModule(this))
            .build()
            .inject(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //TODO Need to handle this event in the new menu fragment
                EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_MENU);
                //drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Timber.d("onNewIntent %s", intent.toString());
        super.onNewIntent(intent);
        this.setIntent(intent);
        checkDeepLinks();
        setIntentAction(intent);
        if (mainFragment != null) {
            if (getIntent()!=null && getIntent().getAction() != null) {
                switch (getIntent().getAction()) {
                    case ConfirmationFragment.KEY_RESUME_TOUR:
                    case ConfirmationFragment.KEY_END_TOUR:
                        BusProvider.getInstance().post(new OnCheckIntentActionEvent());
                        break;
                    case TourService.KEY_NOTIFICATION_STOP_TOUR:
                    case TourService.KEY_NOTIFICATION_PAUSE_TOUR:
                    case TourService.KEY_LOCATION_PROVIDER_DISABLED:
                        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                        break;
                    default:
                        break;
                }
            }
            sendMapFragmentExtras();
        }

        if (getIntent()==null || getIntent().getAction() == null) {
            // user just returns to the app, update analytics
            updateAnalyticsInfo();
        }
    }

    @Override
    public void onBackPressed() {
        if (mainFragment instanceof BackPressable) {
            BackPressable backPressable = (BackPressable) mainFragment;
            if (!backPressable.onBackPressed()) {
                finish();
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onStart() {
        BusProvider.getInstance().register(this);
        presenter.checkForUpdate();

        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent() != null) {
            String action = getIntent().getAction();
            if (action != null) {
                if (TourService.KEY_LOCATION_PROVIDER_DISABLED.equals(action)) {
                    displayLocationProviderDisabledAlert();
                    sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                } else if (TourService.KEY_NOTIFICATION_PAUSE_TOUR.equals(action) || TourService.KEY_NOTIFICATION_STOP_TOUR.equals(action)) {
                    sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                }
            }
        }
        EntourageApplication.get().getMixpanel().getPeople().showNotificationIfAvailable(this);

        refreshBadgeCount();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        checkIntentAction(null);
    }

    @Override
    protected void onStop() {
        BusProvider.getInstance().unregister(this);
        super.onStop();
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void displayLocationProviderDisabledAlert() {
        if(LocationUtils.INSTANCE.isLocationEnabled() && LocationUtils.INSTANCE.isLocationPermissionGranted()) {
            Timber.i("No need to ask for permission: false alert...");
            return;
        }
        try {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.error_dialog_disabled))
                    .setCancelable(false)
                    .setPositiveButton("Oui", (dialogInterface, i) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("Non", (dialogInterface, i) -> dialogInterface.cancel())
                    .create()
                    .show();
        } catch(Exception e) {
            Timber.e(e);
        }
    }

    private void sendMapFragmentExtras() {
        AuthenticationController authenticationController = getAuthenticationController();
        if (authenticationController == null || authenticationController.getUser() == null) return;
        int userId = authenticationController.getUser().getId();
        boolean choice = authenticationController.isUserToursOnly();
        mapEntourageFragment.onNotificationExtras(userId, choice);
    }

    private void setIntentAction(Intent intent) {
        Bundle args = intent.getExtras();
        if (args != null) {
            intentTour = (Tour) args.getSerializable(Tour.KEY_TOUR);
            if (args.getBoolean(ConfirmationFragment.KEY_RESUME_TOUR, false)) {
                getIntent().setAction(ConfirmationFragment.KEY_RESUME_TOUR);
                return;
            } else if (args.getBoolean(ConfirmationFragment.KEY_END_TOUR, false)) {
                getIntent().setAction(ConfirmationFragment.KEY_END_TOUR);
                return;
            }
        }
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case PushNotificationContent.TYPE_NEW_CHAT_MESSAGE :
                case PushNotificationContent.TYPE_NEW_JOIN_REQUEST:
                case PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED:
                case PushNotificationContent.TYPE_ENTOURAGE_INVITATION:
                case PushNotificationContent.TYPE_INVITATION_STATUS:
                case TourService.KEY_LOCATION_PROVIDER_DISABLED:
                case TourService.KEY_NOTIFICATION_PAUSE_TOUR:
                case TourService.KEY_NOTIFICATION_STOP_TOUR:
                    getIntent().setAction(intent.getAction());
                    break;
                default:
                    getIntent().setAction(null);
            }
        }
    }

    public void popToMapFragment() {
        if (mapEntourageFragment != null) {
            mapEntourageFragment.dismissAllDialogs();
        }
    }

    private void configureToolbar() {
        if (bottomBar != null) {
            // we need to set the listener fist, to respond to the default selected tab request
            bottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int menuId = item.getItemId();
                    if(menuId == navigationDataSource.getActionMenuId()) {
                        showFeed(true);
                        //don't load any fragments and don't switch menu
                        return false;
                    }
                    hideAction();
                    loadFragment(menuId);
                    return true;
                }
            });

            bottomBar.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
                @Override
                public void onNavigationItemReselected(@NonNull MenuItem item) {
                    if(item.getItemId() == navigationDataSource.getFeedTabIndex()) {
                        hideAction();
                    }
                }
            });

            int defaultId = navigationDataSource.getDefaultSelectedTab();
            loadFragment(defaultId);
            bottomBar.setSelectedItemId(defaultId);
        }
    }

    protected void selectNavigationTab(int menuIndex) {
        if (bottomBar != null && bottomBar.getSelectedItemId()  != menuIndex) {
            bottomBar.setSelectedItemId(menuIndex);
        }
    }

    public void selectItem(@IdRes int menuId) {
        if (menuId == 0) {
            return;
        }
        if (presenter != null) {
            presenter.handleMenu(menuId);
        }
        selectedSidemenuAction = 0;
    }

    protected void loadFragment(int menuId) {
        try {
            Fragment newFragment = navigationDataSource.getFragmentAtIndex(menuId);
            if (newFragment == null) return;
            mainFragment = newFragment;
            if (mainFragment instanceof MapEntourageFragment) {
                mapEntourageFragment = (MapEntourageFragment) newFragment;
            }
            String tag = navigationDataSource.getFragmentTagAtIndex(menuId);
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (!fragmentManager.popBackStackImmediate(tag, 0)) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_fragment, mainFragment, tag);
                fragmentTransaction.addToBackStack(tag);
                fragmentTransaction.commit();

            }
        } catch(IllegalStateException e){
            EntourageEvents.logEvent(EntourageEvents.EVENT_ILLEGAL_STATE);
        }
    }

    public void showMapFilters() {
        if (mapEntourageFragment != null) {
            mapEntourageFragment.onShowFilter();
        }
    }

    public void hideAction() {
        if (mainFragment instanceof MapEntourageFragment) {
            mapEntourageFragment.hideAction();
        }
    }

    public void showFeed(boolean showActionMenu) {
        selectNavigationTab(navigationDataSource.getFeedTabIndex());
        if(showActionMenu) {
            popToMapFragment();
            onShowAction();
        }
    }

    public void showGuide() {
        selectNavigationTab(navigationDataSource.getGuideTabIndex());
    }

    public void showMyEntourages() {
        selectNavigationTab(navigationDataSource.getMyMessagesTabIndex());
    }

    public void showTutorial() {
        showTutorial(false);
    }

    public void showTutorial(boolean forced) {
        if (presenter != null) {
            presenter.displayTutorial(forced);
        }
    }

    private void initializePushNotifications() {
        final SharedPreferences sharedPreferences = EntourageApplication.get().getSharedPreferences();
        boolean notificationsEnabled = sharedPreferences.getBoolean(EntourageApplication.KEY_NOTIFICATIONS_ENABLED, true);
        if (notificationsEnabled) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( this, instanceIdResult -> presenter.updateApplicationInfo(instanceIdResult.getToken()));
        } else {
            presenter.deleteApplicationInfo();
        }
    }

    private void updateAnalyticsInfo() {
        User user = getAuthenticationController().getUser();
        if (user == null) return;
        EntourageEvents.updateUserInfo(user, getApplicationContext(), NotificationManagerCompat.from(this).areNotificationsEnabled());
    }

    @Override
    protected void logout() {
        if (mapEntourageFragment != null) {
            mapEntourageFragment.saveOngoingTour();
        }
        //remove user phone
        final SharedPreferences sharedPreferences = EntourageApplication.get().getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        User me = EntourageApplication.me(getApplicationContext());
        if(me != null) {
            HashSet<String> loggedNumbers = (HashSet<String>) sharedPreferences.getStringSet(EntourageApplication.KEY_TUTORIAL_DONE, new HashSet<>());
            loggedNumbers.remove(me.getPhone());
            editor.putStringSet(EntourageApplication.KEY_TUTORIAL_DONE, loggedNumbers);
        }

        presenter.deleteApplicationInfo();

        editor.remove(EntourageApplication.KEY_REGISTRATION_ID);
        editor.remove(EntourageApplication.KEY_NOTIFICATIONS_ENABLED);
        editor.remove(EntourageApplication.KEY_GEOLOCATION_ENABLED);
        editor.apply();

        super.logout();
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void GCMTokenObtained(OnGCMTokenObtainedEvent event) {
        presenter.updateApplicationInfo(event.getRegistrationId());
    }

    @Subscribe
    public void checkIntentAction(OnCheckIntentActionEvent event) {
        if (!isSafeToCommit()) return;

        Intent intent = getIntent();
        if (intent == null) {
            intentTour = null;
            return;
        }

        if(mapEntourageFragment!= null) {
            mapEntourageFragment.checkAction(intent.getAction(), intentTour);
        }
        Message message = null;
        if (intent.getExtras() != null) {
            message = (Message) intent.getExtras().getSerializable(PushNotificationManager.PUSH_MESSAGE);
        }
        if (message != null) {
            PushNotificationContent content = message.getContent();
            if (content != null) {
                PushNotificationContent.Extra extra = content.extra;
                switch(intent.getAction()) {
                    case PushNotificationContent.TYPE_NEW_CHAT_MESSAGE:
                        if (content.isTourRelated()) {
                            mapEntourageFragment.displayChosenFeedItem(content.getJoinableUUID(), TimestampedObject.TOUR_CARD);
                        } else if (content.isEntourageRelated()) {
                            mapEntourageFragment.displayChosenFeedItem(content.getJoinableUUID(), TimestampedObject.ENTOURAGE_CARD);
                        } else {
                            showMyEntourages();
                        }
                        break;
                    case PushNotificationContent.TYPE_NEW_JOIN_REQUEST:
                    case PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED:
                        if (content.isTourRelated()) {
                            mapEntourageFragment.displayChosenFeedItem(content.getJoinableUUID(), TimestampedObject.TOUR_CARD);
                        } else if (content.isEntourageRelated()) {
                            mapEntourageFragment.displayChosenFeedItem(content.getJoinableUUID(), TimestampedObject.ENTOURAGE_CARD);
                        }
                        break;
                    case PushNotificationContent.TYPE_ENTOURAGE_INVITATION:
                        if (extra != null) {
                            mapEntourageFragment.displayChosenFeedItem(String.valueOf(extra.entourageId), TimestampedObject.ENTOURAGE_CARD, extra.invitationId);
                        }
                        break;
                    case PushNotificationContent.TYPE_INVITATION_STATUS:
                        if (extra != null && (content.isEntourageRelated() || content.isTourRelated())) {
                            mapEntourageFragment.displayChosenFeedItem(content.getJoinableUUID(),
                                    content.isTourRelated() ? TimestampedObject.TOUR_CARD : TimestampedObject.ENTOURAGE_CARD);
                        }
                        break;
                }
            }
            EntourageApplication application = EntourageApplication.get();
            if (application != null) {
                application.removePushNotification(message);
            }
            refreshBadgeCount();
        } else {
            // Handle the deep link
            DeepLinksManager.getInstance().handleCurrentDeepLink(this);
        }
        intentTour = null;
        setIntent(null);
    }

    @Subscribe
    public void userViewRequested(OnUserViewRequestedEvent event) {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_USERPROFILE);
        try {
            UserFragment fragment = UserFragment.newInstance(event.getUserId());
            fragment.show(getSupportFragmentManager(), UserFragment.TAG);
        } catch (IllegalStateException e) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ILLEGAL_STATE);
            Timber.e(e);
        }
    }

    @Subscribe
    public void feedItemViewRequested(OnFeedItemInfoViewRequestedEvent event) {
        if (mapEntourageFragment != null && event != null) {
            FeedItem feedItem = event.getFeedItem();
            if (feedItem != null) {
                mapEntourageFragment.displayChosenFeedItem(feedItem, event.getfeedRank());
                //refresh badge count
                refreshBadgeCount();
                // update the newsfeed card
                mapEntourageFragment.onPushNotificationConsumedForFeedItem(feedItem);
                // update the my entourages card, if necessary
                MyEntouragesFragment myEntouragesFragment = (MyEntouragesFragment) getSupportFragmentManager().findFragmentByTag(MyEntouragesFragment.TAG);
                if (myEntouragesFragment != null) {
                    myEntouragesFragment.onPushNotificationConsumedForFeedItem(feedItem);
                }
            } else {
                //check if we are receiving feed type and id
                int feedItemType = event.getFeedItemType();
                String feedItemUUID = event.getFeedItemUUID();
                if (feedItemType == 0) {
                    return;
                }
                if (feedItemUUID == null || feedItemUUID.length() == 0) {
                    String shareURL = event.getFeedItemShareURL();
                    mapEntourageFragment.displayChosenFeedItemFromShareURL(shareURL, feedItemType);
                } else {
                    mapEntourageFragment.displayChosenFeedItem(feedItemUUID, feedItemType, event.getInvitationId());
                }
            }
        }
    }

    @Subscribe
    public void userActRequested(final OnUserActEvent event) {
        if (OnUserActEvent.ACT_JOIN.equals(event.getAct())) {
            if (mapEntourageFragment != null) {
                mapEntourageFragment.act(event.getFeedItem());
            }
        } else if (OnUserActEvent.ACT_QUIT.equals(event.getAct())) {
            if (mapEntourageFragment == null) {
                Toast.makeText(DrawerActivity.this, R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show();
            } else {
                User me = EntourageApplication.me(DrawerActivity.this);
                if (me == null) {
                    Toast.makeText(DrawerActivity.this, R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show();
                } else {
                    FeedItem item = event.getFeedItem();
                    if (item != null && FeedItem.JOIN_STATUS_PENDING.equals(item.getJoinStatus())) {
                        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_CANCEL_JOIN_REQUEST);
                    }
                    mapEntourageFragment.removeUserFromNewsfeedCard(item, me.getId());
                }
            }
            /*
            FeedItem feedItem = event.getFeedItem();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            int titleId = R.string.tour_info_quit_tour_title;
            int messageId = R.string.tour_info_quit_tour_description;
            if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
                titleId = R.string.entourage_info_quit_entourage_title;
                messageId = R.string.entourage_info_quit_entourage_description;
            }
            builder.setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (mapEntourageFragment == null) {
                            Toast.makeText(DrawerActivity.this, R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show();
                        } else {
                            User me = EntourageApplication.me(DrawerActivity.this);
                            if (me == null) {
                                Toast.makeText(DrawerActivity.this, R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show();
                            } else {
                                FeedItem item = event.getFeedItem();
                                if (item != null && FeedItem.JOIN_STATUS_PENDING.equals(item.getJoinStatus())) {
                                    EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_CANCEL_JOIN_REQUEST);
                                }
                                mapEntourageFragment.removeUserFromNewsfeedCard(item, me.getId());
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.no, null);
            builder.create().show();
            */
        }
    }

    @Subscribe
    public void tourEncounterViewRequested(OnTourEncounterViewRequestedEvent event) {
        Encounter encounter = event.getEncounter();
        if (encounter == null) {
            return;
        }
        if (encounter.isReadOnly()) {
            Intent intent = new Intent(this, ReadEncounterActivity.class);
            Bundle extras = new Bundle();
            extras.putSerializable(ReadEncounterActivity.BUNDLE_KEY_ENCOUNTER, encounter);
            intent.putExtras(extras);
            this.startActivity(intent);
        } else {
            Intent intent = new Intent(this, CreateEncounterActivity.class);
            Bundle extras = new Bundle();
            extras.putSerializable(CreateEncounterActivity.BUNDLE_KEY_ENCOUNTER, encounter);
            intent.putExtras(extras);
            this.startActivity(intent);
        }
    }

    @Subscribe
    public void feedItemCloseRequested(OnFeedItemCloseRequestEvent event) {
        FeedItem feedItem = event.getFeedItem();
        if (feedItem == null) {
            return;
        }
        if (mapEntourageFragment != null) {
            if (event.isShowUI()) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_ACTIVE_CLOSE_OVERLAY);
                presenter.displayFeedItemOptions(feedItem);
                return;
            }
            // Only the author can close entourages/tours
            User me = EntourageApplication.me(this);
            if (me == null || feedItem.getAuthor() == null) {
                return;
            }
            int myId = me.getId();
            if (feedItem.getAuthor().getUserID() != myId) {
                return;
            }

            if (!feedItem.isClosed()) {
                // close
                mapEntourageFragment.stopFeedItem(feedItem, event.isSuccess());
            } else if (feedItem.getType() == TimestampedObject.TOUR_CARD && !feedItem.isFreezed()) {
                // freeze
                mapEntourageFragment.freezeTour((Tour) feedItem);
            }
        }
    }

    @Subscribe
    public void onUnauthorized(OnUnauthorizedEvent event) {
        logout();
    }

    @Subscribe
    public void onLocationPermissionGranted(Events.OnLocationPermissionGranted event) {
        if (event == null) return;

        EntourageEvents.onLocationPermissionGranted(event.isPermissionGranted());
    }

    @Subscribe
    public void onShowURLRequested(Events.OnShowURLEvent event) {
        if (event == null) return;
        showWebView(event.getUrl());
    }

    @Subscribe
    public void onUserUpdateEvent(Events.OnUserInfoUpdatedEvent event) {
        updateAnalyticsInfo();
    }

    @Override
    public void closeEntourageInformationFragment(EntourageInformationFragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(fragment).commit();
    }

    @Override
    public void showStopTourActivity(Tour tour) {
        mapEntourageFragment.pauseTour(tour);

        ConfirmationFragment confirmationFragment = ConfirmationFragment.newInstance(tour);
        confirmationFragment.show(getSupportFragmentManager(), ConfirmationFragment.TAG);
    }

    @Override
    public void closeChoiceFragment(ChoiceFragment fragment, Tour tour) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(fragment).commit();
        if (tour != null) {
            if (mainFragment instanceof MapEntourageFragment) {
                MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) mainFragment;
                mapEntourageFragment.displayChosenFeedItem(tour, 0);
            }
        }
    }

    @Override
    public void onEntourageDisclaimerAccepted(final EntourageDisclaimerFragment fragment) {
        // Save the entourage disclaimer shown flag
        User me = EntourageApplication.me(this);
        me.setEntourageDisclaimerShown(true);
        getAuthenticationController().saveUser(me);

        // Dismiss the disclaimer fragment
        if (fragment != null) fragment.dismiss();

        // Show the create entourage fragment
        if (mainFragment instanceof MapEntourageFragment) {
            ((MapEntourageFragment) mainFragment).createEntourage();
        }
    }

    @Override
    public void onEncounterDisclaimerAccepted(EncounterDisclaimerFragment fragment) {
        // Save the entourage disclaimer shown flag
        User me = EntourageApplication.me(this);
        me.setEncounterDisclaimerShown(true);
        getAuthenticationController().saveUser(me);

        // Dismiss the disclaimer fragment
        fragment.dismiss();

        if (mainFragment instanceof MapEntourageFragment) {
            ((MapEntourageFragment) mainFragment).addEncounter();
        }
    }

    @Override
    public void onPhotoBack() {
        // Do nothing
    }

    @Override
    public void onPhotoIgnore() {
        // Do nothing
    }

    @Override
    public void onPhotoChosen(final Uri photoUri, int photoSource) {

        if (photoSource == PhotoChooseSourceFragment.TAKE_PHOTO_REQUEST) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_PHOTO_SUBMIT);
        }

        //Upload the photo to Amazon S3
        showProgressDialog(R.string.user_photo_uploading);

        File file = new File(photoUri.getPath());
        avatarUploadPresenter.uploadPhoto(file);
    }

    public void onUploadError() {
        Toast.makeText(DrawerActivity.this, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show();
        dismissProgressDialog();
        PhotoEditFragment photoEditFragment = (PhotoEditFragment) getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
        if (photoEditFragment != null) {
            photoEditFragment.onPhotoSent(false);
        }
    }

    // ----------------------------------
    // Floating Actions handling
    // ----------------------------------

    public void onCreateEntourageDeepLink() {
        if (mainFragment instanceof MapEntourageFragment) {
            mapEntourageFragment.displayEntourageDisclaimer();
        }
    }

    public void onShowAction() {
        if (mainFragment instanceof MapEntourageFragment) {
            mapEntourageFragment.onShowAction();
        }
    }

    // ----------------------------------
    // PUSH NOTIFICATION HANDLING
    // ----------------------------------

    @Subscribe
    public void onPushNotificationReceived(OnPushNotificationReceived event) {
        final Message message = event.getMessage();
        if (message == null || message.getContent() == null || message.getContent().getJoinableId() == 0) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            PushNotificationContent content = message.getContent();
            if (content == null  || content.getType()==null) {
                return;
            }
            String contentType = content.getType();
            switch (contentType) {
                case PushNotificationContent.TYPE_NEW_CHAT_MESSAGE:
                    if (displayMessageOnCurrentTourInfoFragment(message)) {
                        //already displayed
                        removePushNotification(content, contentType);
                    } else {
                        addPushNotification(message);
                    }
                    break;
                case PushNotificationContent.TYPE_JOIN_REQUEST_CANCELED:
                    //@todo should we update current tour info fragment ?
                    removePushNotification(content, PushNotificationContent.TYPE_NEW_JOIN_REQUEST);
                    break;
                case PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED:
                    addPushNotification(message);
                    if (mapEntourageFragment != null) {
                        mapEntourageFragment.userStatusChanged(content, Tour.JOIN_STATUS_ACCEPTED);
                    }
                    break;
                default:
                    /*TYPE_NEW_JOIN_REQUEST,TYPE_ENTOURAGE_INVITATION,TYPE_INVITATION_STATUS*/
                    addPushNotification(message);
                    break;
            }
        });
    }

    private void removePushNotification(PushNotificationContent content, String contentType) {
        EntourageApplication application = EntourageApplication.get();
        if (application != null) {
            if (content.isTourRelated()) {
                application.removePushNotification(content.getJoinableId(), TimestampedObject.TOUR_CARD, content.getUserId(), contentType);
            } else if (content.isEntourageRelated()) {
                application.removePushNotification(content.getJoinableId(), TimestampedObject.ENTOURAGE_CARD, content.getUserId(), contentType);
            }
        }
    }

    private boolean displayMessageOnCurrentTourInfoFragment(@NonNull Message message) {
        EntourageInformationFragment fragment = (EntourageInformationFragment) getSupportFragmentManager().findFragmentByTag(EntourageInformationFragment.TAG);
        return fragment != null && fragment.onPushNotificationChatMessageReceived(message);
    }

    private void addPushNotification(Message message) {
        if (mapEntourageFragment != null) {
            mapEntourageFragment.onPushNotificationReceived(message);
        }
        MyEntouragesFragment myEntouragesFragment = (MyEntouragesFragment) getSupportFragmentManager().findFragmentByTag(MyEntouragesFragment.TAG);
        if (myEntouragesFragment != null) {
            myEntouragesFragment.onPushNotificationReceived(message);
        }
        refreshBadgeCount();
    }

    // ----------------------------------
    // ACTION ZONE HANDLING
    // ----------------------------------

    public void showEditActionZoneFragment() {
        showEditActionZoneFragment( null);
    }

    public void showEditActionZoneFragment(UserEditActionZoneFragment.FragmentListener extraFragmentListener) {
        AuthenticationController authenticationController = EntourageApplication.get().getEntourageComponent().getAuthenticationController();
        if (!authenticationController.isAuthenticated()) {
            return;
        }
        User me = authenticationController.getUser();
        if(me==null) {
            return;
        }

        UserPreferences userPreferences = authenticationController.getUserPreferences();
        if(userPreferences== null) {
            return;
        }

        boolean noNeedToShowEditScreen = me.isEditActionZoneShown()
                || userPreferences.isIgnoringActionZone()
                || (me.getAddress()!=null
                    && me.getAddress().getDisplayAddress() != null
                    && me.getAddress().getDisplayAddress().length() > 0
        );
        if (noNeedToShowEditScreen) {
            return;
        }

        UserEditActionZoneFragment userEditActionZoneFragment = UserEditActionZoneFragment.newInstance(null);
        userEditActionZoneFragment.addFragmentListener(this);
        userEditActionZoneFragment.addFragmentListener(extraFragmentListener);
        userEditActionZoneFragment.setFromLogin(true);
        userEditActionZoneFragment.show(getSupportFragmentManager(), UserEditActionZoneFragment.TAG);
        me.setEditActionZoneShown(true);
    }

    @Override
    public void onUserEditActionZoneFragmentDismiss() {

    }

    @Override
    public void onUserEditActionZoneFragmentIgnore() {
        storeActionZone(true);
    }

    @Override
    public void onUserEditActionZoneFragmentAddressSaved() {
        storeActionZone(false);
    }

    private void storeActionZone(final boolean ignoreZone) {
        AuthenticationController authenticationController = EntourageApplication.get().getEntourageComponent().getAuthenticationController();
        if (authenticationController.isAuthenticated()) {
            UserPreferences userPreferences = authenticationController.getUserPreferences();
            if (userPreferences != null) {
                userPreferences.setIgnoringActionZone(ignoreZone);
                authenticationController.saveUserPreferences();
            }
        }
        UserEditActionZoneFragment fragment = (UserEditActionZoneFragment)getSupportFragmentManager().findFragmentByTag(UserEditActionZoneFragment.TAG);
        if (fragment != null && !fragment.isStateSaved()) {
            fragment.dismiss();
        }
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void onMyEntouragesForceRefresh(Events.OnMyEntouragesForceRefresh event) {
        FeedItem item = event.getFeedItem();
        if(item!=null) {
            EntourageApplication application = EntourageApplication.get();
            if (application != null) {
                application.updateBadgeCountForFeedItem(item);
            }
            refreshBadgeCount();
        }
    }

    // ----------------------------------
    // Helper functions
    // ----------------------------------

    private void refreshBadgeCount() {
        if(messageBadge==null) {
            if(bottomBar!=null){
                messageBadge = bottomBar.getOrCreateBadge(navigationDataSource.getMyMessagesTabIndex());
                messageBadge.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.map_announcement_background, null));
                messageBadge.setBadgeTextColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
                messageBadge.setMaxCharacterCount(2);
            }
        }
        if (messageBadge!= null) {
            int badgeCount = EntourageApplication.get().getBadgeCount();
            if(badgeCount > 0) {
                messageBadge.setVisible(true);
                messageBadge.setNumber(badgeCount);

            } else {
                messageBadge.setVisible(false);
            }
        }
    }

}
