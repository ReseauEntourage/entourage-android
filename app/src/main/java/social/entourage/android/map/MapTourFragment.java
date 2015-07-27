package social.entourage.android.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.common.Constants;

/**
 * Created by NTE on 13/07/15.
 */
public class MapTourFragment extends Fragment {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private OnTourActionListener callback;
    private boolean isPaused;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public static MapTourFragment newInstance() {
        return new MapTourFragment();
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (OnTourActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTourActionListener");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @OnClick(R.id.tour_stop_button)
    public void stopTour(View view) {
        callback.onTourPaused();
    }

    @OnClick(R.id.tour_add_encounter_button)
    public void addEncounter(View view) {
        callback.onNewEncounter();
    }

    // ----------------------------------
    // INTERFACES
    // ----------------------------------

    public interface OnTourActionListener {
        void onTourPaused();
        void onPausedTourResumed();
        void onNewEncounter();
    }

}
