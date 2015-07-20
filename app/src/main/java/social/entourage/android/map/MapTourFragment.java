package social.entourage.android.map;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.R;

/**
 * Created by NTE on 13/07/15.
 */
public class MapTourFragment extends Fragment {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private OnTourActionListener callback;
    private boolean isPaused;

    @InjectView(R.id.tour_pause_button)
    Button pauseButton;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public static MapTourFragment newInstance() {
        MapTourFragment fragment = new MapTourFragment();
        return fragment;
    }

    // ----------------------------------
    // SETTERS
    // ----------------------------------

    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View toReturn = inflater.inflate(R.layout.fragment_map_tour, container, false);
        ButterKnife.inject(this, toReturn);
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(isPaused) pauseButton.setText(R.string.tour_resume);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (OnTourActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTourActionListener");
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @OnClick(R.id.tour_pause_button)
    public void pauseTour(View view) {
        if (!isPaused) {
            pauseButton.setText(R.string.tour_resume);
            isPaused = true;
            callback.onTourPaused();
        } else {
            pauseButton.setText(R.string.tour_pause);
            isPaused = false;
            callback.onPausedTourResumed();
        }
    }

    @OnClick(R.id.tour_stop_button)
    public void stopTour(View view) {
        callback.onTourStopped();
    }

    @OnClick(R.id.tour_add_encounter_button)
    public void addEncounter(View view) {
        callback.onNewEncounter();
    }

    public void switchPauseButton() {
        if (!isPaused) {
            pauseButton.setText(R.string.tour_resume);
            isPaused = true;
        } else {
            pauseButton.setText(R.string.tour_pause);
            isPaused = false;
        }
    }

    // ----------------------------------
    // INTERFACES
    // ----------------------------------

    public interface OnTourActionListener {
        void onTourPaused();
        void onPausedTourResumed();
        void onTourStopped();
        void onNewEncounter();
    }

}
