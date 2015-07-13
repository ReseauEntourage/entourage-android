package social.entourage.android.guide;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.flurry.android.FlurryAgent;

import javax.inject.Inject;

import butterknife.ButterKnife;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Poi;
import social.entourage.android.common.Constants;
import social.entourage.android.login.LoginActivity;
import social.entourage.android.map.MapActivity;

@SuppressWarnings("WeakerAccess")
public class GuideMapActivity extends EntourageSecuredActivity implements ActionBar.TabListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final long UPDATE_TIMER_MILLIS = 1000;
    private static final float DISTANCE_BETWEEN_UPDATES_METERS = 10;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    GuideMapPresenter presenter;

    private Fragment fragment;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        ButterKnife.inject(this);

        FlurryAgent.logEvent(Constants.EVENT_OPEN_GUIDE_FROM_MENU);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(false);

        // TODO: Remove depreceated code : move tabs in toolbar
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab().setText(R.string.activity_map_tab_map).setTabListener(this));
        //TODO: display List Tab here
        //actionBar.addTab(actionBar.newTab().setText(R.string.activity_map_tab_liste).setTabListener(this));
    }

    @Override
    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerGuideMapComponent.builder()
                .entourageComponent(entourageComponent)
                .guideMapModule(new GuideMapModule(this))
                .build()
                .inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_guide, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            getAuthenticationController().logOutUser();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_encounters) {
            saveCameraPosition();
            startActivity(new Intent(this, MapActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        switch (tab.getPosition()) {
            case 0:
                fragment = GuideMapEntourageFragment.newInstance();
                break;
            case 1:
                fragment = GuideListFragment.newInstance();
                break;
        }
        fragmentTransaction.replace(R.id.layout_fragment_container, fragment);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.remove(fragment);
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void putPoi(Poi poi, GuideMapPresenter.OnEntourageMarkerClickListener onClickListener) {
        if (fragment instanceof GuideMapEntourageFragment) {
            GuideMapEntourageFragment guideMapEntourageFragment = (GuideMapEntourageFragment) fragment;
            guideMapEntourageFragment.putPoiOnMap(poi, onClickListener);
        }
    }

    public void setOnMarkerCLickListener(GuideMapPresenter.OnEntourageMarkerClickListener onMarkerClickListener) {
        if (fragment instanceof GuideMapEntourageFragment) {
            GuideMapEntourageFragment guideMapEntourageFragment = (GuideMapEntourageFragment) fragment;
            guideMapEntourageFragment.setOnMarkerClickListener(onMarkerClickListener);
        }
    }

    public void clearMap() {
        if (fragment instanceof GuideMapEntourageFragment) {
            GuideMapEntourageFragment guideMapEntourageFragment = (GuideMapEntourageFragment) fragment;
            guideMapEntourageFragment.clearMap();
        }
    }

    public void initializeMap() {
        if (fragment instanceof GuideMapEntourageFragment) {
            GuideMapEntourageFragment guideMapEntourageFragment = (GuideMapEntourageFragment) fragment;
            guideMapEntourageFragment.initializeMapZoom();
        }
    }

    public void saveCameraPosition() {
        if (fragment instanceof GuideMapEntourageFragment) {
            GuideMapEntourageFragment guideMapEntourageFragment = (GuideMapEntourageFragment) fragment;
            guideMapEntourageFragment.saveCameraPosition();
        }
    }
}