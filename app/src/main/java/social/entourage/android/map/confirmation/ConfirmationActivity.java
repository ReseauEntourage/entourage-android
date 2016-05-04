package social.entourage.android.map.confirmation;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.authentication.login.LoginActivity;

@SuppressWarnings("WeakerAccess")
public class ConfirmationActivity extends EntourageSecuredActivity {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String KEY_END_TOUR = "social.entourage.android.KEY_END_TOUR";
    public static final String KEY_RESUME_TOUR = "social.entourage.android.KEY_RESUME_TOUR";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    ConfirmationPresenter presenter;

    @Bind(R.id.confirmation_encounters)
    TextView encountersView;

    @Bind(R.id.confirmation_distance)
    TextView distanceView;

    @Bind(R.id.confirmation_duration)
    TextView durationView;

    @Bind(R.id.confirmation_resume_button)
    Button resumeButton;

    @Bind(R.id.confirmation_end_button)
    Button endButton;

    private Tour tour;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(false);

        setContentView(R.layout.layout_map_confirmation);
        ButterKnife.bind(this);

        if (!getAuthenticationController().isAuthenticated()) {
            startActivity(new Intent(this, LoginActivity.class));
        }

        tour = (Tour) getIntent().getExtras().getSerializable(Tour.KEY_TOUR);
        initializeView();
    }

    @Override
    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerConfirmationComponent.builder()
                .entourageComponent(entourageComponent)
                .confirmationModule(new ConfirmationModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onBackPressed() {
        onResumeTour();
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeView() {
        if (tour != null) {
            Resources res = getResources();
            int encountersCount = tour.getEncounters().size();
            int distanceInt = (int) tour.getDistance();
            String distanceString = String.format("%d:%d", distanceInt/1000, distanceInt % 1000);
            encountersView.setText(""+encountersCount);
            distanceView.setText(distanceString);
            durationView.setText(tour.getDuration());
        }
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------

    @OnClick(R.id.confirmation_resume_button)
    public void onResumeTour() {
        Bundle args = new Bundle();
        args.putBoolean(KEY_RESUME_TOUR, true);
        args.putSerializable(Tour.KEY_TOUR, tour);
        Intent resumeIntent = new Intent(this, DrawerActivity.class);
        resumeIntent.putExtras(args);
        startActivity(resumeIntent);
    }

    @OnClick(R.id.confirmation_end_button)
    public void onEndTour() {
        Bundle args = new Bundle();
        args.putBoolean(KEY_END_TOUR, true);
        args.putSerializable(Tour.KEY_TOUR, tour);
        Intent resumeIntent = new Intent(this, DrawerActivity.class);
        resumeIntent.putExtras(args);
        startActivity(resumeIntent);
    }
}
