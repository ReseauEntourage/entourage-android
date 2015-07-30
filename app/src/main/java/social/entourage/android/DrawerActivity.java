package social.entourage.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import social.entourage.android.guide.GuideMapEntourageFragment;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.map.tour.TourService;

public class DrawerActivity extends EntourageSecuredActivity {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @InjectView(R.id.navigation_view)
    NavigationView navigationView;

    @InjectView(R.id.content_view)
    View contentView;

    @InjectView(R.id.drawer_header_user)
    TextView userView;

    private Fragment mainFragment;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        mainFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        ButterKnife.inject(this);

        userView.setText(getAuthenticationController().getUser().getFirstName());

        configureToolbar();
        configureNavigationItem();
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
        if (intent.getBooleanExtra(TourService.NOTIFICATION_PAUSE, false)) {
            loadFragment(new MapEntourageFragment());
            if (mainFragment instanceof MapEntourageFragment) {
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) mainFragment;
                        mapEntourageFragment.onNotificationAction(TourService.NOTIFICATION_PAUSE);
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mainFragment instanceof BackPressable) {
            BackPressable backPressable = (BackPressable) mainFragment;
            if (!backPressable.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
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
                        selectItem(menuItem);
                    }
                });
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void selectItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_tours:
                loadFragment(new MapEntourageFragment());
                break;
            case R.id.action_guide:
                loadFragment(new GuideMapEntourageFragment());
                break;
            case R.id.action_logout:
                logout();
                break;
            default:
                Snackbar.make(contentView, getString(R.string.drawer_error, menuItem.getTitle()), Snackbar.LENGTH_LONG).show();
        }
    }

    private void loadFragment(Fragment newFragment) {
        mainFragment = newFragment;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment, mainFragment);
        fragmentTransaction.commit();
    }
}
