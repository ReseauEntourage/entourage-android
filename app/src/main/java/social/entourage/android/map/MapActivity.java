package social.entourage.android.map;

import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Poi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * Created by RPR on 25/03/15.
 */
public class MapActivity extends EntourageSecuredActivity implements ActionBar.TabListener {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    MapPresenter presenter;

    private Fragment fragment;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected List<Object> getScopedModules() {
        return Arrays.<Object>asList(new MapModule(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.inject(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(false);

        // TODO: Remove depreceated code : move tabs in toolbar
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab().setText(R.string.activity_map_tab_map).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.activity_map_tab_liste).setTabListener(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        switch (tab.getPosition()) {
            case 0:
                fragment = MapEntourageFragment.newInstance();
                break;
            case 1:
                fragment = ListFragment.newInstance();
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

    public void putEncouter(Encounter encounter, MapPresenter.OnEntourageMarkerClickListener onClickListener) {
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.putEncounterOnMap(encounter, onClickListener);
        }
    }

    public void putPoi(Poi poi, MapPresenter.OnEntourageMarkerClickListener onClickListener) {
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.putPoiOnMap(poi, onClickListener);
        }
    }

    public void setOnMarkerCLickListener(MapPresenter.OnEntourageMarkerClickListener onMarkerClickListener) {
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.setOnMarkerClickListener(onMarkerClickListener);
        }
    }
}