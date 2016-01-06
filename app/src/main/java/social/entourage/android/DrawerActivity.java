package social.entourage.android;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.guide.GuideMapEntourageFragment;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.map.choice.ChoiceFragment;
import social.entourage.android.map.confirmation.ConfirmationActivity;
import social.entourage.android.map.tour.TourInformationFragment;
import social.entourage.android.message.push.RegisterGCMService;
import social.entourage.android.user.UserActivity;

public class DrawerActivity extends EntourageSecuredActivity implements TourInformationFragment.OnTourInformationFragmentFinish, ChoiceFragment.OnChoiceFragmentFinish {

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

        Picasso.with(this).load(R.drawable.ic_user_photo)
                .transform(new CropCircleTransformation())
                .into(userPhoto);

        User user = getAuthenticationController().getUser();
        if (user != null) {
            userName.setText(user.getFirstName());
        }

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
        Bundle args = intent.getExtras();
        int userId = getAuthenticationController().getUser().getId();
        boolean choice = getAuthenticationController().isUserToursOnly();
        if (args != null) {
            if (args.getBoolean(ConfirmationActivity.KEY_RESUME_TOUR, false)) {
                if (mainFragment instanceof MapEntourageFragment) {
                    mapEntourageFragment = (MapEntourageFragment) mainFragment;
                    mapEntourageFragment.onNotificationExtras(userId, choice, ConfirmationActivity.KEY_RESUME_TOUR);
                } else {
                    loadFragmentWithExtras(ConfirmationActivity.KEY_RESUME_TOUR);
                }
            } else if (args.getBoolean(ConfirmationActivity.KEY_END_TOUR, false)) {
                if (mainFragment instanceof MapEntourageFragment) {
                    mapEntourageFragment = (MapEntourageFragment) mainFragment;
                    mapEntourageFragment.onNotificationExtras(userId, choice, ConfirmationActivity.KEY_END_TOUR);
                } else {
                    loadFragmentWithExtras(ConfirmationActivity.KEY_END_TOUR);
                }
            }
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
        if (mainFragment instanceof MapEntourageFragment) {
            navigationView.getMenu().getItem(0).setChecked(true);
        }
        else if (mainFragment instanceof GuideMapEntourageFragment) {
            navigationView.getMenu().getItem(1).setChecked(true);
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

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
                //if (mapEntourageFragment == null) {
                    mapEntourageFragment = (MapEntourageFragment) getSupportFragmentManager().findFragmentByTag("fragment_map");
                    if (mapEntourageFragment == null) {
                        mapEntourageFragment = new MapEntourageFragment();
                    }
                //}
                loadFragmentWithExtras(null);
                break;
            case R.id.action_guide:
                //if (guideMapEntourageFragment == null) {
                    guideMapEntourageFragment = (GuideMapEntourageFragment) getSupportFragmentManager().findFragmentByTag("fragment_guide");
                    if (guideMapEntourageFragment == null) {
                        guideMapEntourageFragment = new GuideMapEntourageFragment();
                    }
                //}
                loadFragment(guideMapEntourageFragment, "fragment_guide");
                break;
            case R.id.action_user:
                startActivity(new Intent(this, UserActivity.class));
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
        fragmentTransaction.commit();
    }

    private void loadFragmentWithExtras(final String action) {
        MapEntourageFragment fragment = (MapEntourageFragment) getSupportFragmentManager().findFragmentByTag("fragment_map");
        if (fragment == null) {
            fragment = new MapEntourageFragment();
        }
        loadFragment(fragment, "fragment_map");
        if (getAuthenticationController().getUser() != null) {
            final int userId = getAuthenticationController().getUser().getId();
            final boolean choice = getAuthenticationController().isUserToursOnly();
            if (mainFragment instanceof MapEntourageFragment) {
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) mainFragment;
                        mapEntourageFragment.onNotificationExtras(userId, choice, action);
                    }
                });
            }
        }
    }

    // ----------------------------------
    // ONCLICK CALLBACKS
    // ----------------------------------

    @OnClick(R.id.drawer_header_user_photo)
    void openUserProfile() {
        startActivity(new Intent(this, UserActivity.class));
        drawerLayout.closeDrawers();
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
