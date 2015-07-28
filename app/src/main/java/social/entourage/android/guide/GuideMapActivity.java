package social.entourage.android.guide;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.flurry.android.FlurryAgent;

import javax.inject.Inject;

import butterknife.ButterKnife;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Poi;
import social.entourage.android.Constants;
import social.entourage.android.authentication.login.LoginActivity;
import social.entourage.android.map.MapActivity;

@SuppressWarnings("WeakerAccess")
public class GuideMapActivity extends EntourageSecuredActivity {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    GuideMapPresenter presenter;

    private GuideMapEntourageFragment guideMapFragment;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_guide);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ButterKnife.inject(this);

        guideMapFragment = (GuideMapEntourageFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);

        FlurryAgent.logEvent(Constants.EVENT_OPEN_GUIDE_FROM_MENU);
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

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void putPoi(Poi poi, GuideMapPresenter.OnEntourageMarkerClickListener onClickListener) {
        guideMapFragment.putPoiOnMap(poi, onClickListener);
    }

    public void setOnMarkerCLickListener(GuideMapPresenter.OnEntourageMarkerClickListener onMarkerClickListener) {
        guideMapFragment.setOnMarkerClickListener(onMarkerClickListener);
    }

    public void clearMap() {
        guideMapFragment.clearMap();
    }

    public void initializeMap() {
        guideMapFragment.initializeMapZoom();
    }

    public void saveCameraPosition() {
        guideMapFragment.saveCameraPosition();
    }

}