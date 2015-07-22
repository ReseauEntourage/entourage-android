package social.entourage.android.map;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.R;

/**
 * Created by NTE on 21/07/15.
 */
public class MapConfirmationFragment extends Fragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String KEY_ENCOUNTERS = "encounters";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_DURATION = "duration";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private OnTourConfirmationListener callback;

    @InjectView(R.id.confirmation_encounters)
    TextView encountersView;

    @InjectView(R.id.confirmation_distance)
    TextView distanceView;

    @InjectView(R.id.confirmation_duration)
    TextView durationView;

    @InjectView(R.id.confirmation_resume_button)
    Button resumeButton;

    @InjectView(R.id.confirmation_end_button)
    Button endButton;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public static MapConfirmationFragment newInstance() {
        return new MapConfirmationFragment();
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View toReturn = inflater.inflate(R.layout.fragment_map_confirmation, container, false);
        ButterKnife.inject(this, toReturn);
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        encountersView.setText(getString(R.string.tour_end_encounters, getArguments().getInt(KEY_ENCOUNTERS)));
        distanceView.setText(getString(R.string.tour_end_distance, getArguments().getFloat(KEY_DISTANCE)));
        distanceView.setText(getString(R.string.tour_end_distance, String.format("%.1f", getArguments().getFloat(KEY_DISTANCE)/1000)));
        durationView.setText(getString(R.string.tour_end_duration, getArguments().getString(KEY_DURATION)));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (OnTourConfirmationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTourConfirmationListener");
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @OnClick(R.id.confirmation_resume_button)
    public void resumeTour(View view) {
        callback.onTourConfirmationResume();
    }

    @OnClick(R.id.confirmation_end_button)
    public void endTour(View view) {
        callback.onTourConfirmationEnd();
    }

    // ----------------------------------
    // INTERFACES
    // ----------------------------------

    public interface OnTourConfirmationListener {
        void onTourConfirmationResume();
        void onTourConfirmationEnd();
    }
}
