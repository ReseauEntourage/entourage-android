package social.entourage.android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.tape.event.CheckIntentActionEvent;
import social.entourage.android.guide.GuideMapEntourageFragment;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.map.choice.ChoiceFragment;
import social.entourage.android.map.confirmation.ConfirmationActivity;
import social.entourage.android.map.tour.TourInformationFragment;
import social.entourage.android.map.tour.TourService;
import social.entourage.android.message.push.RegisterGCMService;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.user.UserFragment;

public class DrawerActivity extends EntourageSecuredActivity implements TourInformationFragment.OnTourInformationFragmentFinish, ChoiceFragment.OnChoiceFragmentFinish {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final String TAG_FRAGMENT_MAP = "fragment_map";
    private final String TAG_FRAGMENT_GUIDE = "fragment_guide";
    private final String TAG_FRAGMENT_USER = "fragment_user";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

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

    private Fragment mainFragment;
    private MapEntourageFragment mapEntourageFragment;
    private GuideMapEntourageFragment guideMapEntourageFragment;
    private UserFragment userFragment;

    private String intentAction;

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

        intentAction = getIntent().getAction();

        Picasso.with(this).load(R.drawable.ic_user_photo)
                .transform(new CropCircleTransformation())
                .into(userPhoto);

        User user = getAuthenticationController().getUser();
        if (user != null) {
            userName.setText(user.getFirstName());
        }

        BusProvider.getInstance().register(this);
        startService(new Intent(this, RegisterGCMService.class));
    }

    @Override
    protected void setupComponent(EntourageComponent entourageComponent) {
        entourageComponent.inject(this);
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
    protected void onResume() {
        super.onResume();
        highlightCurrentMenuItem();

        String action = getIntent().getAction();
        if (action != null) {
            if (TourService.KEY_GPS_DISABLED.equals(action)) {
                displayAlertNoGps();
                sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            }
            else if (TourService.KEY_NOTIFICATION_PAUSE_TOUR.equals(action)) {
                sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            }
        }
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
                            DrawerActivity.this.finish();
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
    }

    private void configureNavigationItem() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem menuItem) {
                menuItem.setChecked(true);
                drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        selectItem(menuItem.getItemId());
                    }
                });
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void selectItem(@IdRes int menuId) {
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
                loadFragment(userFragment, TAG_FRAGMENT_USER);
                break;
            case R.id.action_logout:
                logout();
                break;
            default:
                //Snackbar.make(contentView, getString(R.string.drawer_error, menuItem.getTitle()), Snackbar.LENGTH_LONG).show();
        }
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

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void checkIntentAction(CheckIntentActionEvent event) {
        switchToMapFragment();
        mapEntourageFragment.checkAction(intentAction);
        intentAction = null;
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
}
