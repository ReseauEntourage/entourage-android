package social.entourage.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.tour.Encounter;
import social.entourage.android.api.model.feed.FeedItem;
import social.entourage.android.api.model.tour.Tour;
import social.entourage.android.api.tape.Events;
import social.entourage.android.api.tape.Events.OnCheckIntentActionEvent;
import social.entourage.android.api.tape.Events.OnGCMTokenObtainedEvent;
import social.entourage.android.api.tape.Events.OnPushNotificationReceived;
import social.entourage.android.api.tape.Events.OnTourEncounterViewRequestedEvent;
import social.entourage.android.api.tape.Events.OnUnauthorizedEvent;
import social.entourage.android.api.tape.Events.OnUserViewRequestedEvent;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.base.BackPressable;
import social.entourage.android.base.EntourageSecuredActivity;
import social.entourage.android.configuration.Configuration;
import social.entourage.android.deeplinks.DeepLinksManager;
import social.entourage.android.entourage.EntourageDisclaimerFragment;
import social.entourage.android.entourage.information.EntourageInformationFragment;
import social.entourage.android.map.filter.MapFilter;
import social.entourage.android.map.filter.MapFilterFactory;
import social.entourage.android.tools.log.EntourageEvents;
import social.entourage.android.user.edit.UserEditActionZoneFragment;
import social.entourage.android.onboarding.OnboardingPhotoFragment;
import social.entourage.android.user.edit.UserEditActionZoneFragmentCompat;
import social.entourage.android.tour.TourInformationFragment;
import social.entourage.android.entourage.my.MyEntouragesFragment;
import social.entourage.android.location.EntourageLocation;
import social.entourage.android.location.LocationUtils;
import social.entourage.android.newsfeed.BaseNewsfeedFragment;
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
import social.entourage.android.user.edit.photo.PhotoChooseInterface;
import social.entourage.android.user.edit.photo.PhotoEditFragment;
import timber.log.Timber;

public class MainActivity extends EntourageSecuredActivity
    implements TourInformationFragment.OnTourInformationFragmentFinish,
    ChoiceFragment.OnChoiceFragmentFinish,
    EntourageDisclaimerFragment.OnFragmentInteractionListener,
    EncounterDisclaimerFragment.OnFragmentInteractionListener,
    PhotoChooseInterface,
    UserEditActionZoneFragment.FragmentListener,
    AvatarUploadView {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    MainPresenter presenter;

    @Inject
    AvatarUploadPresenter avatarUploadPresenter;

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomBar;

    //Tooltips
    @BindView(R.id.ui_layout_tooltips)
    ConstraintLayout ui_layout_tooltips_main;

    @BindView(R.id.ui_layout_tooltips_ignore)
    ConstraintLayout ui_layout_tooltips_ignore;

    @BindView(R.id.ui_tooltip_layout_top)
    ConstraintLayout ui_tooltip_layout_top;
    @BindView(R.id.ui_tooltip_layout_bottom)
    ConstraintLayout ui_tooltip_layout_bottom;

    @BindView(R.id.ui_tooltip_button_filter)
    View ui_tooltip_button_filter;

    @BindView(R.id.ui_tooltip_button_next_top)
    Button ui_tooltip_button_next_top;

    @BindView(R.id.ui_tooltip_tv_title)
    TextView ui_tooltip_tv_title;
    @BindView(R.id.ui_tooltip_tv_step)
    TextView ui_tooltip_tv_step;
    @BindView(R.id.ui_tooltip_tv_bottom)
    TextView ui_tooltip_tv_bottom;
    @BindView(R.id.ui_tooltip_button_next_bottom)
    Button ui_tooltip_button_next_bottom;
    @BindView(R.id.ui_tooltip_iv_bottom1)
    ImageView ui_tooltip_iv_bottom1;
    @BindView(R.id.ui_tooltip_iv_bottom2)
    ImageView ui_tooltip_iv_bottom2;
    @BindView(R.id.ui_tooltip_iv_bottom_bt1)
    ImageView ui_tooltip_iv_bottom_bt1;
    @BindView(R.id.ui_tooltip_iv_bottom_bt2)
    ImageView ui_tooltip_iv_bottom_bt2;
    private int postionTooltip = 0;


    private BottomNavigationDataSource navigationDataSource;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (isFinishing()) return;

        configureBottombar();

        if (getIntent() != null) {
            DeepLinksManager.INSTANCE.storeIntent(getIntent());
        }

        if(getAuthenticationController().isAuthenticated()) {
            //refresh the user info from the server
            Location location = EntourageLocation.getCurrentLocation();
            presenter.updateUserLocation(location);
            //initialize the push notifications
            initializePushNotifications();

            updateAnalyticsInfo();
        }
    }

    @Override
    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerMainComponent.builder()
            .entourageComponent(entourageComponent)
            .mainModule(new MainModule(this))
            .build()
            .inject(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Timber.d("onNewIntent %s", intent.toString());
        super.onNewIntent(intent);
        this.setIntent(intent);
        DeepLinksManager.INSTANCE.storeIntent(intent);
        setIntentAction(intent);
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        if ((currentFragment instanceof BackPressable) && ((BackPressable) currentFragment).onBackPressed()) {
            //backAction is done in the fragment
            return;
        }
        finish();
    }

    @Override
    protected void onStart() {
        BusProvider.INSTANCE.getInstance().register(this);
        presenter.checkForUpdate();

        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent() != null && getIntent().getAction()!=null) {
            switch(getIntent().getAction()) {
                case EntourageService.KEY_LOCATION_PROVIDER_DISABLED:
                    displayLocationProviderDisabledAlert();
                    sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                    break;
                case EntourageService.KEY_NOTIFICATION_PAUSE_TOUR:
                case EntourageService.KEY_NOTIFICATION_STOP_TOUR:
                    sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                    break;
            }
        }

        sendNewsfeedFragmentExtras();
        if (getIntent()==null || getIntent().getAction() == null) {
            // user just returns to the app, update analytics
            updateAnalyticsInfo();
        }
        refreshBadgeCount();

        if (getIntent()!=null && getIntent().getAction() != null) {
            BusProvider.INSTANCE.getInstance().post(new OnCheckIntentActionEvent(getIntent().getAction(), getIntent().getExtras()));
        }

        checkOnboarding();
    }

    @Override
    protected void onStop() {
        BusProvider.INSTANCE.getInstance().unregister(this);
        super.onStop();
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void checkOnboarding() {
        final SharedPreferences sharedPreferences = EntourageApplication.get().getSharedPreferences();
        boolean isFromOnboarding = sharedPreferences.getBoolean(EntourageApplication.KEY_IS_FROM_ONBOARDING, false);

        if (isFromOnboarding) {
            sharedPreferences.edit().putBoolean(EntourageApplication.KEY_IS_FROM_ONBOARDING,false).apply();

            ui_tooltip_layout_bottom.setVisibility(View.INVISIBLE);
            ui_tooltip_iv_bottom2.setVisibility(View.INVISIBLE);
            ui_tooltip_iv_bottom1.setVisibility(View.INVISIBLE);
            ui_tooltip_iv_bottom_bt1.setVisibility(View.INVISIBLE);
            ui_tooltip_iv_bottom_bt2.setVisibility(View.INVISIBLE);
            ui_layout_tooltips_main.setVisibility(View.VISIBLE);

            ui_layout_tooltips_ignore.setOnClickListener(v -> {
                ui_layout_tooltips_main.setVisibility(View.GONE);
                switch (postionTooltip) {
                    case 0:
                        EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_TOOLTIP_FILTER_CLOSE);
                        break;
                    case 1:
                        EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_TOOLTIP_GUIDE_CLOSE);
                        break;
                    default:
                        EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_TOOLTIP_PLUS_CLOSE);
                        break;
                }
            });
            ui_tooltip_button_next_top.setOnClickListener(v -> {
                ui_tooltip_layout_top.setVisibility(View.GONE);
                ui_tooltip_button_filter.setVisibility(View.GONE);

                String _txt = String.format(getString(R.string.tooltip_step_format),"2");
                ui_tooltip_tv_step.setText(_txt);
                ui_tooltip_tv_bottom.setText(R.string.tooltip_desc2);
                ui_tooltip_tv_title.setText(R.string.tooltip_title2);

                ui_tooltip_iv_bottom1.setVisibility(View.VISIBLE);
                ui_tooltip_iv_bottom2.setVisibility(View.INVISIBLE);
                ui_tooltip_iv_bottom_bt1.setVisibility(View.VISIBLE);
                ui_tooltip_iv_bottom_bt2.setVisibility(View.INVISIBLE);
                ui_tooltip_layout_bottom.setVisibility(View.VISIBLE);
                EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_TOOLTIP_FILTER_NEXT);
            });
            ui_tooltip_button_next_bottom.setOnClickListener(v -> {
                postionTooltip++;
                if (postionTooltip == 1) {
                    String _txt = String.format(getString(R.string.tooltip_step_format),"3");
                    ui_tooltip_tv_step.setText(_txt);
                    ui_tooltip_tv_bottom.setText(R.string.tooltip_desc3);
                    ui_tooltip_tv_title.setText(R.string.tooltip_title3);
                    ui_tooltip_iv_bottom2.setVisibility(View.VISIBLE);
                    ui_tooltip_iv_bottom1.setVisibility(View.INVISIBLE);
                    ui_tooltip_iv_bottom_bt1.setVisibility(View.INVISIBLE);
                    ui_tooltip_iv_bottom_bt2.setVisibility(View.VISIBLE);
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_TOOLTIP_GUIDE_NEXT);
                }
                else {
                    ui_layout_tooltips_main.setVisibility(View.GONE);
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_TOOLTIP_PLUS_NEXT);
                }
            });

            int usertype = sharedPreferences.getInt(EntourageApplication.KEY_ONBOARDING_USER_TYPE,0);
            setupFiltersAfterOnboarding(usertype);
        }
        else {
            ui_layout_tooltips_main.setVisibility(View.GONE);
        }
    }

    private void setupFiltersAfterOnboarding(int userType) {
        MapFilter mapFilter = MapFilterFactory.getMapFilter();
        switch (userType) {
            case 1: //Neighbour
                mapFilter.setNeighbourFilters();
                break;
            case 2: //Alone
                mapFilter.setAloneFilters();
                break;
            case 3: //Asso
            default:
                mapFilter.setDefaultValues();
        }

        EntourageApplication.get().entourageComponent.getAuthenticationController().saveMapFilter();
        BusProvider.INSTANCE.getInstance().post(new Events.OnMapFilterChanged());
    }

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

    @Nullable
    private BaseNewsfeedFragment getNewsfeedFragment() {
        return (BaseNewsfeedFragment) getSupportFragmentManager().findFragmentByTag(BaseNewsfeedFragment.TAG);
    }

    private void sendNewsfeedFragmentExtras() {
        AuthenticationController authenticationController = getAuthenticationController();
        if (authenticationController == null || authenticationController.getMe() == null) return;
        BaseNewsfeedFragment newsfeedFRagment  = getNewsfeedFragment();
        if(newsfeedFRagment !=null) {
            newsfeedFRagment.onNotificationExtras(authenticationController.getMe().id, authenticationController.isUserToursOnly());
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
                    //we keep the action
                    //getIntent().setAction(intent.getAction());
                    break;
                default:
                    //we get rid of the action
                    //@TODO is it necessary ?
                    //getIntent().setAction(null);
            }
        }
    }

    public void dismissNewsfeedFragmentDialogs() {
        BaseNewsfeedFragment newsfeedFragment  = getNewsfeedFragment();
        if(newsfeedFragment !=null) {
            newsfeedFragment.dismissAllDialogs();
        }
    }

    private void configureBottombar() {
        if(navigationDataSource == null) {
            navigationDataSource = new BottomNavigationDataSource();
        }
        if (bottomBar != null) {
            // we need to set the listener fist, to respond to the default selected tab request
            bottomBar.setOnNavigationItemSelectedListener(item -> {
                if(shouldBypassNavigation(item.getItemId())) {
                    return false;
                }
                if(bottomBar.getSelectedItemId()!=item.getItemId()) {
                    loadFragment(item.getItemId());
                }
                return true;
            });

            int defaultId = navigationDataSource.getDefaultSelectedTab();
            //bottomBar.setSelectedItemId(defaultId);
            loadFragment(defaultId);

            BadgeDrawable messageBadge = bottomBar.getOrCreateBadge(navigationDataSource.getMyMessagesTabIndex());
            messageBadge.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.map_announcement_background, null));
            messageBadge.setBadgeTextColor(ResourcesCompat.getColor(getResources(), R.color.primary, null));
            messageBadge.setMaxCharacterCount(2);

        }
    }

    private boolean shouldBypassNavigation(@IdRes int itemId) {
        if(itemId==navigationDataSource.getActionMenuId()) {
            //Handling special cases
            if (!Configuration.INSTANCE.showPlusScreen()) {
                // Show directly the create entourage disclaimer
                createEntourage();
                return true;
            }
            else if (getAuthenticationController() != null && getAuthenticationController().getSavedTour()!=null) {
                // Show directly the create encounter
                //TODO should be bound to service
                addEncounter();
                return true;
            }
        }
        return false;
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
            //TODO check if we need to execute pending actions
            //fragmentManager.executePendingTransactions();
        } catch(IllegalStateException e){
            Timber.w(e);
        }
    }

    public void showMapFilters() {
        BaseNewsfeedFragment mapFragment  = getNewsfeedFragment();
        if(mapFragment !=null) {
            mapFragment.onShowFilter();
        }
    }

    public void showFeed() {
        selectNavigationTab(navigationDataSource.getFeedTabIndex());
    }

    public void showGuide() {
        selectNavigationTab(navigationDataSource.getGuideTabIndex());
    }

    public void showEvents() {
        selectNavigationTab(navigationDataSource.getFeedTabIndex());
        BaseNewsfeedFragment mapFragment  = getNewsfeedFragment();
        if(mapFragment !=null) {
            mapFragment.onShowEvents();
        }
    }

    public void showMyEntourages() {
        selectNavigationTab(navigationDataSource.getMyMessagesTabIndex());
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
        User user = getAuthenticationController().getMe();
        if (user == null) return;
        EntourageEvents.updateUserInfo(user, getApplicationContext(), NotificationManagerCompat.from(this).areNotificationsEnabled());
    }

    @Override
    protected void logout() {
        BaseNewsfeedFragment mapFragment  = getNewsfeedFragment();
        if(mapFragment !=null) {
            mapFragment.saveOngoingTour();
        }
        //remove user phone
        final SharedPreferences sharedPreferences = EntourageApplication.get().getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        User me = getAuthenticationController().getMe();
        if(me != null) {
            HashSet<String> loggedNumbers = (HashSet<String>) sharedPreferences.getStringSet(EntourageApplication.KEY_TUTORIAL_DONE, new HashSet<>());
            loggedNumbers.remove(me.phone);
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
    public void checkIntentAction(@NotNull OnCheckIntentActionEvent event) {
        //if (!isSafeToCommit()) return;

        Message message = null;
        if (event.getExtras() != null) {
            message = (Message) event.getExtras().getSerializable(PushNotificationManager.PUSH_MESSAGE);
        }
        if (message != null) {
            PushNotificationContent content = message.getContent();
            if (content != null
                    && PushNotificationContent.TYPE_NEW_CHAT_MESSAGE.equals(event.getAction())
                    && !content.isTourRelated()
                    && !content.isEntourageRelated()) {
                showMyEntourages();
            }
            EntourageApplication.get().removePushNotification(message);
            refreshBadgeCount();
        } else {
            // Handle the deep link
            DeepLinksManager.INSTANCE.handleCurrentDeepLink(this);
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
            Timber.w(e);
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
    public void showStopTourActivity(@NotNull Tour tour) {
        BaseNewsfeedFragment mapFragment  = getNewsfeedFragment();
        if(mapFragment !=null) {
            mapFragment.pauseTour(tour);
        }

        TourEndConfirmationFragment tourEndConfirmationFragment = TourEndConfirmationFragment.newInstance(tour);
        tourEndConfirmationFragment.show(getSupportFragmentManager(), TourEndConfirmationFragment.TAG);
    }

    @Override
    public void closeChoiceFragment(ChoiceFragment fragment, Tour tour) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(fragment).commit();
        if (tour != null) {
            BaseNewsfeedFragment mapFragment  = getNewsfeedFragment();
            if(mapFragment !=null) {
                mapFragment.displayChosenFeedItem(tour, 0);
            }
        }
    }

    @Override
    public void onEntourageDisclaimerAccepted(final EntourageDisclaimerFragment fragment) {
        // Save the entourage disclaimer shown flag
        try{
            getAuthenticationController().setEntourageDisclaimerShown(true);

            // Dismiss the disclaimer fragment
            if (fragment != null) fragment.dismiss();

            // Show the create entourage fragment
            BaseNewsfeedFragment mapFragment  = getNewsfeedFragment();
            if(mapFragment !=null) {
                mapFragment.createEntourage();
            }
        } catch (IllegalStateException e) {
            Timber.w(e);
        }

    }

    @Override
    public void onEncounterDisclaimerAccepted(EncounterDisclaimerFragment fragment) {
        // Save the entourage disclaimer shown flag
        getAuthenticationController().setEncounterDisclaimerShown(true);

        // Dismiss the disclaimer fragment
        fragment.dismiss();

        BaseNewsfeedFragment mapFragment  = getNewsfeedFragment();
        if(mapFragment !=null) {
            mapFragment.addEncounter();
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

        if (photoSource == OnboardingPhotoFragment.TAKE_PHOTO_REQUEST) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_PHOTO_SUBMIT);
        }

        //Upload the photo to Amazon S3
        showProgressDialog(R.string.user_photo_uploading);

        String path = photoUri.getPath();
        if(path == null) return;
        File file = new File(path);
        avatarUploadPresenter.uploadPhoto(file);
    }

    public void onUploadError() {
        Toast.makeText(MainActivity.this, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show();
        dismissProgressDialog();
        PhotoEditFragment photoEditFragment = (PhotoEditFragment) getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
        if (photoEditFragment != null) {
            photoEditFragment.onPhotoSent(false);
        }
    }

    // ----------------------------------
    // Deeplink actions handling
    // ----------------------------------

    public void createEntourage() {
        showFeed();
        dismissNewsfeedFragmentDialogs();
        BaseNewsfeedFragment mapFragment  = getNewsfeedFragment();
        if(mapFragment !=null) {
            mapFragment.displayEntourageDisclaimer();
        }
    }

    public void addEncounter() {
        showFeed();
        dismissNewsfeedFragmentDialogs();
        BaseNewsfeedFragment mapFragment  = getNewsfeedFragment();
        if(mapFragment !=null) {
            mapFragment.onAddEncounter();
        }
    }

    // ----------------------------------
    // PUSH NOTIFICATION HANDLING
    // ----------------------------------

    @Subscribe
    public void onPushNotificationReceived(OnPushNotificationReceived event) {
        final Message message = event.getMessage();
        if (message == null) {
            return;
        }
        PushNotificationContent content = message.getContent();
        if (content == null || content.getJoinableId() == 0) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            if (content.getType() == null) {
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
                    //@TODO should we update current tour info fragment ?
                    removePushNotification(content, PushNotificationContent.TYPE_NEW_JOIN_REQUEST);
                    break;
                case PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED:
                    addPushNotification(message);
                    BaseNewsfeedFragment mapFragment  = getNewsfeedFragment();
                    if(mapFragment !=null) {
                        mapFragment.userStatusChanged(content, Tour.JOIN_STATUS_ACCEPTED);
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
        if (content.isTourRelated()) {
            application.removePushNotification(content.getJoinableId(), TimestampedObject.TOUR_CARD, content.getUserId(), contentType);
        } else if (content.isEntourageRelated()) {
            application.removePushNotification(content.getJoinableId(), TimestampedObject.ENTOURAGE_CARD, content.getUserId(), contentType);
        }
    }

    private boolean displayMessageOnCurrentTourInfoFragment(@NonNull Message message) {
        EntourageInformationFragment fragment = (EntourageInformationFragment) getSupportFragmentManager().findFragmentByTag(EntourageInformationFragment.TAG);
        return fragment != null && fragment.onPushNotificationChatMessageReceived(message);
    }

    private void addPushNotification(Message message) {
        BaseNewsfeedFragment mapFragment  = getNewsfeedFragment();
        if(mapFragment !=null) {
            mapFragment.onPushNotificationReceived(message);
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
        showEditActionZoneFragment( null,false);
    }

    public void showEditActionZoneFragment(UserEditActionZoneFragment.FragmentListener extraFragmentListener,Boolean isSecondaryAddress) {
        if (!getAuthenticationController().isAuthenticated()) {
            return;
        }
        User me = getAuthenticationController().getMe();
        if(me==null || ( me.address !=null && me.address.displayAddress.length() > 0)) {
            return;
        }

        if(getAuthenticationController().getEditActionZoneShown() || getAuthenticationController().isIgnoringActionZone()) {
            return; //noNeedToShowEditScreen
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            UserEditActionZoneFragmentCompat userEditActionZoneFragmentCompat = UserEditActionZoneFragmentCompat.newInstance(null,false);
            userEditActionZoneFragmentCompat.addFragmentListener(this);
            userEditActionZoneFragmentCompat.addFragmentListener(extraFragmentListener);
            userEditActionZoneFragmentCompat.setFromLogin(true);
            userEditActionZoneFragmentCompat.show(getSupportFragmentManager(), UserEditActionZoneFragmentCompat.TAG);
        }
        else {
            UserEditActionZoneFragment userEditActionZoneFragment = UserEditActionZoneFragment.newInstance(null,isSecondaryAddress);
            userEditActionZoneFragment.setupListener(this);
            userEditActionZoneFragment.setupListener(extraFragmentListener);
            userEditActionZoneFragment.show(getSupportFragmentManager(), UserEditActionZoneFragment.TAG);
        }

        getAuthenticationController().setEditActionZoneShown(true);
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
        if (getAuthenticationController().isAuthenticated()) {
            getAuthenticationController().setIgnoringActionZone(ignoreZone);
        }
        UserEditActionZoneFragmentCompat fragment = (UserEditActionZoneFragmentCompat)getSupportFragmentManager().findFragmentByTag(UserEditActionZoneFragmentCompat.TAG);
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
            EntourageApplication.get().updateBadgeCountForFeedItem(item);
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
