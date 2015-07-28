package social.entourage.android.map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.map.tour.TourService;

public class MapActivity extends EntourageSecuredActivity {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private MapEntourageFragment mapFragment;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setTitle(R.string.activity_map_title);
        mapFragment = (MapEntourageFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void setupComponent(EntourageComponent entourageComponent) {
        entourageComponent.inject(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getBooleanExtra(TourService.NOTIFICATION_PAUSE, false)) {
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mapFragment.onNotificationAction(TourService.NOTIFICATION_PAUSE);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (!mapFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
