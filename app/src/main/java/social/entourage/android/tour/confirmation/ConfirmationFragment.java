package social.entourage.android.tour.confirmation;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.DrawerActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.base.EntourageDialogFragment;

@SuppressWarnings("WeakerAccess")
public class ConfirmationFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = ConfirmationFragment.class.getSimpleName();

    public static final String KEY_END_TOUR = "social.entourage.android.KEY_END_TOUR";
    public static final String KEY_RESUME_TOUR = "social.entourage.android.KEY_RESUME_TOUR";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.confirmation_encounters)
    TextView encountersView;

    @BindView(R.id.confirmation_distance)
    TextView distanceView;

    @BindView(R.id.confirmation_duration)
    TextView durationView;

    @BindView(R.id.confirmation_resume_button)
    Button resumeButton;

    @BindView(R.id.confirmation_end_button)
    Button endButton;

    private Tour tour;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public ConfirmationFragment() {
        // Required empty public constructor
    }

    public static ConfirmationFragment newInstance(Tour tour) {
        ConfirmationFragment fragment = new ConfirmationFragment();
        Bundle args = new Bundle();
        args.putSerializable(Tour.KEY_TOUR, tour);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_map_confirmation, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            tour = (Tour) args.getSerializable(Tour.KEY_TOUR);
        }
        initializeView();
    }

    @Override
    protected int getSlideStyle() {
        return R.style.CustomDialogFragmentSlide;
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
            encountersView.setText(res.getString(R.string.encounter_count_format, encountersCount));
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
        Intent resumeIntent = new Intent(getActivity(), DrawerActivity.class);
        resumeIntent.putExtras(args);
        startActivity(resumeIntent);
        dismiss();
    }

    @OnClick(R.id.confirmation_end_button)
    public void onEndTour() {
        Bundle args = new Bundle();
        args.putBoolean(KEY_END_TOUR, true);
        args.putSerializable(Tour.KEY_TOUR, tour);
        Intent resumeIntent = new Intent(getActivity(), DrawerActivity.class);
        resumeIntent.putExtras(args);
        startActivity(resumeIntent);
        dismiss();
    }
}
