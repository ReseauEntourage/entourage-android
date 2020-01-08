package social.entourage.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import social.entourage.android.entourage.EntourageDisclaimerFragment;
import social.entourage.android.entourage.information.EntourageInformationFragment;
import social.entourage.android.entourage.my.MyEntouragesFragment;
import social.entourage.android.location.LocationUtils;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.message.push.PushNotificationManager;
import social.entourage.android.navigation.BottomNavigationDataSource;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.service.EntourageService;
import social.entourage.android.tour.choice.ChoiceFragment;
import social.entourage.android.tour.confirmation.TourEndConfirmationFragment;
import social.entourage.android.tour.encounter.CreateEncounterActivity;
import social.entourage.android.tour.encounter.EncounterDisclaimerFragment;
import social.entourage.android.tour.encounter.ReadEncounterActivity;
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

    private BottomNavigationDataSource navigationDataSource;

    protected Fragment mainFragment;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        ButterKnife.bind(this);

        if (isFinishing()) return;

        configureBottombar();

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
    protected void onNewIntent(Intent intent) {
        Timber.d("onNewIntent %s", intent.toString());
        super.onNewIntent(intent);
        this.setIntent(intent);
        checkDeepLinks();
        setIntentAction(intent);
        if (getIntent()!=null && getIntent().getAction() != null) {
            switch (getIntent().getAction()) {
                case TourEndConfirmationFragment.KEY_RESUME_TOUR:
                case TourEndConfirmationFragment.KEY_END_TOUR:
                case PlusFragment.KEY_START_TOUR:
                case PlusFragment.KEY_ADD_ENCOUNTER:
                case PlusFragment.KEY_CREATE_CONTRIBUTION:
                case PlusFragment.KEY_CREATE_DEMAND:
                case PlusFragment.KEY_CREATE_OUTING:
                    BusProvider.getInstance().post(new OnCheckIntentActionEvent());
                    break;
                case EntourageService.KEY_NOTIFICATION_STOP_TOUR:
                case EntourageService.KEY_NOTIFICATION_PAUSE_TOUR:
                case EntourageService.KEY_LOCATION_PROVIDER_DISABLED:
                    sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                    break;
                default:
                    break;
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
                if (EntourageService.KEY_LOCATION_PROVIDER_DISABLED.equals(action)) {
                    displayLocationProviderDisabledAlert();
                    sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                } else if (EntourageService.KEY_NOTIFICATION_PAUSE_TOUR.equals(action) || EntourageService.KEY_NOTIFICATION_STOP_TOUR.equals(action)) {
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
        if(mainFragment instanceof MapEntourageFragment) {
            AuthenticationController authenticationController = getAuthenticationController();
            if (authenticationController == null || authenticationController.getUser() == null) return;
            ((MapEntourageFragment)mainFragment).onNotificationExtras(authenticationController.getUser().getId(), authenticationController.isUserToursOnly());
        }
    }

    private void setIntentAction(Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case PushNotificationContent.TYPE_NEW_CHAT_MESSAGE :
                case PushNotificationContent.TYPE_NEW_JOIN_REQUEST:
                case PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED:
                case PushNotificationContent.TYPE_ENTOURAGE_INVITATION:
                case PushNotificationContent.TYPE_INVITATION_STATUS:
                case EntourageService.KEY_LOCATION_PROVIDER_DISABLED:
                case EntourageService.KEY_NOTIFICATION_PAUSE_TOUR:
                case EntourageService.KEY_NOTIFICATION_STOP_TOUR:
                case TourEndConfirmationFragment.KEY_RESUME_TOUR:
                case TourEndConfirmationFragment.KEY_END_TOUR:
                case PlusFragment.KEY_START_TOUR:
                case PlusFragment.KEY_ADD_ENCOUNTER:
                case PlusFragment.KEY_CREATE_CONTRIBUTION:
                case PlusFragment.KEY_CREATE_DEMAND:
                case PlusFragment.KEY_CREATE_OUTING:
                    getIntent().setAction(intent.getAction());
                    break;
                default:
                    getIntent().setAction(null);
            }
        }
    }

    public void dismissMapFragmentDialogs() {
        if(mainFragment instanceof MapEntourageFragment) {
            ((MapEntourageFragment)mainFragment).dismissAllDialogs();
        }
    }

    private void configureBottombar() {
        if(navigationDataSource == null) {
            navigationDataSource = new BottomNavigationDataSource();
        }
        if (bottomBar != null) {
            // we need to set the listener fist, to respond to the default selected tab request
            bottomBar.setOnNavigationItemSelectedListener(item -> {
                loadFragment(item.getItemId());
                return true;
            });

            int defaultId = navigationDataSource.getDefaultSelectedTab();
            loadFragment(defaultId);
            //bottomBar.setSelectedItemId(defaultId);

            BadgeDrawable messageBadge = bottomBar.getOrCreateBadge(navigationDataSource.getMyMessagesTabIndex());
            messageBadge.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.map_announcement_background, null));
            messageBadge.setBadgeTextColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
            messageBadge.setMaxCharacterCount(2);

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
    }

    protected void loadFragment(int menuId) {
        try {
            String tag = navigationDataSource.getFragmentTagAtIndex(menuId);
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (!fragmentManager.popBackStackImmediate(tag, 0)) {
                Fragment newFragment = navigationDataSource.getFragmentAtIndex(menuId);
                if (newFragment == null) return;
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_fragment, newFragment, tag);
                fragmentTransaction.addToBackStack(tag);
                fragmentTransaction.commit();
            }
            fragmentManager.executePendingTransactions();
            mainFragment = fragmentManager.findFragmentById(R.id.main_fragment);
        } catch(IllegalStateException e){
            EntourageEvents.logEvent(EntourageEvents.EVENT_ILLEGAL_STATE);
        }
    }

    public void showMapFilters() {
        if(mainFragment instanceof MapEntourageFragment) {
            ((MapEntourageFragment)mainFragment).onShowFilter();
        }
    }

    public void showFeed() {
        selectNavigationTab(navigationDataSource.getFeedTabIndex());
    }

    //public void showPlusActions() { selectNavigationTab(navigationDataSource.getActionMenuId()); }

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
        if(mainFragment instanceof MapEntourageFragment) {
            ((MapEntourageFragment)mainFragment).saveOngoingTour();
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
            return;
        }

        Message message = null;
        if (intent.getExtras() != null) {
            message = (Message) intent.getExtras().getSerializable(PushNotificationManager.PUSH_MESSAGE);
        }
        if (message != null) {
            PushNotificationContent content = message.getContent();
            if (content != null
                    && PushNotificationContent.TYPE_NEW_CHAT_MESSAGE.equals(intent.getAction())
                    && !content.isTourRelated()
                    && !content.isEntourageRelated()) {
                showMyEntourages();
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
        if(mainFragment instanceof MapEntourageFragment) {
            ((MapEntourageFragment) mainFragment).pauseTour(tour);
        }

        TourEndConfirmationFragment tourEndConfirmationFragment = TourEndConfirmationFragment.newInstance(tour);
        tourEndConfirmationFragment.show(getSupportFragmentManager(), TourEndConfirmationFragment.TAG);
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
        if(me==null) return;

        me.setEncounterDisclaimerShown(true);
        getAuthenticationController().saveUser(me);

        // Dismiss the disclaimer fragment
        fragment.dismiss();

        if (mainFragment instanceof MapEntourageFragment) {
            ((MapEntourageFragment)mainFragment).addEncounter();
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
        showFeed();
        dismissMapFragmentDialogs();
        if (mainFragment instanceof MapEntourageFragment) {
            ((MapEntourageFragment)mainFragment).displayEntourageDisclaimer();
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
                    if (mainFragment instanceof MapEntourageFragment) {
                        ((MapEntourageFragment)mainFragment).userStatusChanged(content, Tour.JOIN_STATUS_ACCEPTED);
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
        if (mainFragment instanceof MapEntourageFragment) {
            ((MapEntourageFragment)mainFragment).onPushNotificationReceived(message);
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
        if (bottomBar== null) {
            return;
        }
        BadgeDrawable messageBadge = bottomBar.getOrCreateBadge(navigationDataSource.getMyMessagesTabIndex());
        if(messageBadge==null) {
            return;
        }
        int badgeCount = EntourageApplication.get().getBadgeCount();
        if(badgeCount > 0) {
            messageBadge.setVisible(true);
            messageBadge.setNumber(badgeCount);

        } else {
            messageBadge.setVisible(false);
        }
    }
}
