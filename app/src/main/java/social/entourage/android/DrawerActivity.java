package social.entourage.android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.tape.Events.*;
import social.entourage.android.badge.BadgeView;
import social.entourage.android.guide.GuideMapEntourageFragment;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.map.choice.ChoiceFragment;
import social.entourage.android.map.confirmation.ConfirmationActivity;
import social.entourage.android.map.tour.information.TourInformationFragment;
import social.entourage.android.map.tour.TourService;
import social.entourage.android.map.tour.my.MyToursFragment;
import social.entourage.android.message.push.RegisterGCMService;
import social.entourage.android.sidemenu.SideMenuItemView;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.user.UserFragment;

public class DrawerActivity extends EntourageSecuredActivity implements TourInformationFragment.OnTourInformationFragmentFinish, ChoiceFragment.OnChoiceFragmentFinish, MyToursFragment.OnFragmentInteractionListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final String TAG_FRAGMENT_MAP = "fragment_map";
    private final String TAG_FRAGMENT_GUIDE = "fragment_guide";
    private final String TAG_FRAGMENT_USER = "fragment_user";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    DrawerPresenter presenter;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Bind(R.id.navigation_view)
    NavigationView navigationView;

    @Bind(R.id.content_view)
    View contentView;

    @Bind(R.id.drawer_header_user_name)
    TextView userName;

    @Bind(R.id.drawer_header_user_photo)
    ImageView userPhoto;

    @Bind(R.id.toolbar_discussion)
    BadgeView discussionBadgeView;

    private Fragment mainFragment;
    private MapEntourageFragment mapEntourageFragment;
    private GuideMapEntourageFragment guideMapEntourageFragment;
    private UserFragment userFragment;

    private SharedPreferences gcmSharedPreferences;
    private String intentAction;
    private Tour intentTour;

    @IdRes int selectedSidemenuAction;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        ButterKnife.bind(this);

        configureToolbar();
        configureNavigationItem();

        selectItem(R.id.action_tours);

        gcmSharedPreferences = getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE_GCM, Context.MODE_PRIVATE);

        intentAction = getIntent().getAction();

        User user = getAuthenticationController().getUser();
        if (user != null) {
            userName.setText(user.getDisplayName());
            String avatarURL = user.getAvatarURL();
            if (avatarURL != null) {
                Picasso.with(this)
                        .load(Uri.parse(avatarURL))
                        .transform(new CropCircleTransformation())
                        .into(userPhoto);
            }
            //refresh the user info from the server
            Location location = EntourageLocation.getInstance().getCurrentLocation();
            presenter.updateUser(null, null, null, (location != null ? location : null));
            //initialize the push notifications
            initializePushNotifications();
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
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        getIntentAction(intent);
        if (mainFragment != null) {
            switchToMapFragment();
            if (intentAction != null) {
                switch (intentAction) {
                    case ConfirmationActivity.KEY_RESUME_TOUR:
                        break;
                    case ConfirmationActivity.KEY_END_TOUR:
                        break;
                    case TourService.KEY_NOTIFICATION_STOP_TOUR:
                    case TourService.KEY_NOTIFICATION_PAUSE_TOUR:
                        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                        break;
                    case TourService.KEY_GPS_DISABLED:
                        displayAlertNoGps();
                        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                    default:
                        break;
                }
            }
            sendMapFragmentExtras();
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
        super.onStart();
        BusProvider.getInstance().register(this);
        presenter.checkForUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        highlightCurrentMenuItem();

        String action = getIntent().getAction();
        if (action != null) {
            if (TourService.KEY_GPS_DISABLED.equals(action)) {
                displayAlertNoGps();
                sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            }
            else if (TourService.KEY_NOTIFICATION_PAUSE_TOUR.equals(action) || TourService.KEY_NOTIFICATION_STOP_TOUR.equals(action)) {
                sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void displayAlertNoGps() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.error_dialog_disabled))
                    .setCancelable(false)
                    .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .create()
                    .show();
        }
    }

    private void sendMapFragmentExtras() {
        int userId = getAuthenticationController().getUser().getId();
        boolean choice = getAuthenticationController().isUserToursOnly();
        mapEntourageFragment.onNotificationExtras(userId, choice);
    }

    private void getIntentAction(Intent intent) {
        String action = intent.getAction();
        Bundle args = intent.getExtras();
        if (args != null) {
            intentTour = (Tour)args.getSerializable(Tour.KEY_TOUR);
            if (args.getBoolean(ConfirmationActivity.KEY_RESUME_TOUR, false)) {
                intentAction = ConfirmationActivity.KEY_RESUME_TOUR;
            }
            else if (args.getBoolean(ConfirmationActivity.KEY_END_TOUR, false)) {
                intentAction = ConfirmationActivity.KEY_END_TOUR;
            }
        }
        else if (action != null) {
            getIntent().setAction(null);
            if (TourService.KEY_GPS_DISABLED.equals(action)) {
                intentAction = TourService.KEY_GPS_DISABLED;
            }
            else if (TourService.KEY_NOTIFICATION_PAUSE_TOUR.equals(action)) {
                intentAction = TourService.KEY_NOTIFICATION_PAUSE_TOUR;
            }
            else if (TourService.KEY_NOTIFICATION_STOP_TOUR.equals(action)) {
                intentAction = TourService.KEY_NOTIFICATION_STOP_TOUR;
            }
        }
    }

    private void switchToMapFragment() {
        if (mainFragment instanceof MapEntourageFragment) {
            mapEntourageFragment = (MapEntourageFragment) mainFragment;
        } else {
            mapEntourageFragment = (MapEntourageFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MAP);
            loadFragmentWithExtras();
        }
    }

    private void highlightCurrentMenuItem() {
        if (mainFragment instanceof MapEntourageFragment) {
            navigationView.setCheckedItem(R.id.action_tours);
        }
        else if (mainFragment instanceof GuideMapEntourageFragment) {
            navigationView.setCheckedItem(R.id.action_guide);
        }
        else if (mainFragment instanceof UserFragment) {
            navigationView.setCheckedItem(R.id.action_user);
        }
    }

    private void configureToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);

        discussionBadgeView = (BadgeView)toolbar.findViewById(R.id.toolbar_discussion);
        if (discussionBadgeView != null) {
            discussionBadgeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    presenter.displayMyTours();
                }
            });
        }

        //TODO: Remove the following after discussion screen implementation
        Handler badgeTestHandler = new Handler();
        badgeTestHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                discussionBadgeView.setBadgeCount(2);
            }
        }, 5000);
    }

    private void configureNavigationItem() {
        //make the navigation view full screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) navigationView.getLayoutParams();
        params.width = metrics.widthPixels;
        navigationView.setLayoutParams(params);

        //add listener to back button
        ImageView backView = (ImageView) navigationView.findViewById(R.id.drawer_header_back);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                drawerLayout.closeDrawers();
            }
        });

        //add navigationitemlistener
        drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                selectItem(selectedSidemenuAction);
            }
        });

        //add listener to user photo and name, that opens the user profile screen
        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                selectedSidemenuAction = R.id.action_user;
                drawerLayout.closeDrawers();
            }
        });
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                selectedSidemenuAction = R.id.action_user;
                drawerLayout.closeDrawers();
            }
        });

        int childCount = navigationView.getChildCount();
        View v = null;
        for (int i = 0; i < childCount; i++) {
            v = navigationView.getChildAt(i);
            if (v instanceof LinearLayout) {
                int itemsCount = ((LinearLayout) v).getChildCount();
                for (int j = 0; j < itemsCount; j++) {
                    View child = ((LinearLayout) v).getChildAt(j);
                    if (child instanceof SideMenuItemView) {
                        child.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectedSidemenuAction = v.getId();
                                drawerLayout.closeDrawers();
                            }
                        });
                    }
                }
            }
        }
    }

    private void selectItem(@IdRes int menuId) {
        if (menuId == 0) return;;
        switch (menuId) {
            case R.id.action_tours:
                mapEntourageFragment = (MapEntourageFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MAP);
                if (mapEntourageFragment == null) {
                    mapEntourageFragment = new MapEntourageFragment();
                }
                loadFragmentWithExtras();
                break;
            case R.id.action_guide:
                guideMapEntourageFragment = (GuideMapEntourageFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_GUIDE);
                if (guideMapEntourageFragment == null) {
                    guideMapEntourageFragment = new GuideMapEntourageFragment();
                }
                loadFragment(guideMapEntourageFragment, TAG_FRAGMENT_GUIDE);
                break;
            case R.id.action_user:
                userFragment = (UserFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_USER);
                if (userFragment == null) {
                    userFragment = new UserFragment();
                }
                //loadFragment(userFragment, TAG_FRAGMENT_USER);
                userFragment.show(getSupportFragmentManager(), TAG_FRAGMENT_USER);
                break;
            case R.id.action_logout:
                if (mapEntourageFragment != null) {
                    mapEntourageFragment.saveOngoingTour();
                }
                gcmSharedPreferences.edit().remove(RegisterGCMService.KEY_REGISTRATION_ID).commit();
                logout();
                break;
            case R.id.action_settings:
            case R.id.action_about:
            default:
                //Snackbar.make(contentView, getString(R.string.drawer_error, menuItem.getTitle()), Snackbar.LENGTH_LONG).show();
                Toast.makeText(this, R.string.error_not_yet_implemented, Toast.LENGTH_SHORT).show();
        }
        selectedSidemenuAction = 0;
    }

    private void loadFragment(Fragment newFragment, String tag) {
        mainFragment = newFragment;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment, mainFragment, tag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void loadFragmentWithExtras() {
        MapEntourageFragment fragment = (MapEntourageFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MAP);
        if (fragment == null) {
            fragment = new MapEntourageFragment();
        }
        loadFragment(fragment, TAG_FRAGMENT_MAP);
        if (getAuthenticationController().getUser() != null) {
            final int userId = getAuthenticationController().getUser().getId();
            final boolean choice = getAuthenticationController().isUserToursOnly();
            if (mainFragment instanceof MapEntourageFragment) {
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) mainFragment;
                        mapEntourageFragment.onNotificationExtras(userId, choice);
                    }
                });
            }
        }
    }

    private void initializePushNotifications() {
        final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(RegisterGCMService.SHARED_PREFERENCES_FILE_GCM, Context.MODE_PRIVATE);
        boolean notificationsEnabled = sharedPreferences.getBoolean(RegisterGCMService.KEY_NOTIFICATIONS_ENABLED, false);
        if (notificationsEnabled) {
            startService(new Intent(this, RegisterGCMService.class));
        }
        else {
            presenter.updateApplicationInfo("");
        }
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void GCMTokenObtained(OnGCMTokenObtainedEvent event) {
        if (event.getRegistrationId() != null) {
            presenter.updateApplicationInfo(event.getRegistrationId());
        }
        else {
            presenter.updateApplicationInfo("");
        }
    }

    @Subscribe
    public void checkIntentAction(OnCheckIntentActionEvent event) {
        switchToMapFragment();
        mapEntourageFragment.checkAction(intentAction, intentTour);
        intentAction = null;
        intentTour = null;
    }

    @Subscribe
    public void userInfoUpdated(OnUserInfoUpdatedEvent event) {
        User user = getAuthenticationController().getUser();
        if (user != null) {
            userName.setText(user.getDisplayName());
            String avatarURL = user.getAvatarURL();
            if (avatarURL != null) {
                Picasso.with(this)
                        .load(Uri.parse(avatarURL))
                        .transform(new CropCircleTransformation())
                        .into(userPhoto);
            }
        }
    }

    @Subscribe
    public void userViewRequested(OnUserViewRequestedEvent event) {
        UserFragment fragment = UserFragment.newInstance(event.getUserId());
        fragment.show(getSupportFragmentManager(), TAG_FRAGMENT_USER);
    }

    @Subscribe
    public void tourInfoViewRequested(OnTourInfoViewRequestedEvent event) {
        if (mapEntourageFragment != null) {
            mapEntourageFragment.displayChosenTour(event.getTour());
        }
    }

    @Subscribe
    public void userActRequested(OnUserActEvent event) {
        if (event.getAct().equals(OnUserActEvent.ACT_JOIN)) {
            if (mapEntourageFragment != null) {
                mapEntourageFragment.act(event.getTour());
            }
        }
    }

    // ----------------------------------
    // INTERFACES CALLBACKS
    // ----------------------------------

    @Override
    public void closeTourInformationFragment(TourInformationFragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(fragment).commit();
    }

    @Override
    public void showStopTourActivity(Tour tour) {
        mapEntourageFragment.pauseTour(tour);
        //buttonStartLauncher.setVisibility(View.GONE);
        Bundle args = new Bundle();
        args.putSerializable(Tour.KEY_TOUR, tour);
        Intent confirmationIntent = new Intent(this, ConfirmationActivity.class);
        confirmationIntent.putExtras(args);
        startActivity(confirmationIntent);
    }

    @Override
    public void closeChoiceFragment(ChoiceFragment fragment, Tour tour) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(fragment).commit();
        if (tour != null) {
            if (mainFragment instanceof MapEntourageFragment) {
                MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) mainFragment;
                mapEntourageFragment.displayChosenTour(tour);
            }
        }
    }

    @Override
    public void onShowTourInfo(final Tour tour) {

    }
}
