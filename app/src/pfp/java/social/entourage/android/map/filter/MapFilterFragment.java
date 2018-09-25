package social.entourage.android.map.filter;

import android.widget.Switch;

import butterknife.BindView;
import butterknife.OnClick;
import social.entourage.android.R;

public class MapFilterFragment extends BaseMapFilterFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @BindView(R.id.map_filter_entourage_neighborhood_switch)
    Switch entourageNeighborhoodSwitch;

    @BindView(R.id.map_filter_entourage_private_circle_switch)
    Switch entouragePrivateCircleSwitch;

    @BindView(R.id.map_filter_entourage_outing_switch)
    Switch entourageOutingSwitch;

    @BindView(R.id.map_filter_past_events_switch)
    Switch pastEventsSwitch;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public MapFilterFragment() {
        // Required empty public constructor
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------

    @OnClick(R.id.map_filter_entourage_outing_switch)
    protected void onOutingSwitchClicked() {
        if (!entourageOutingSwitch.isChecked()) {
            pastEventsSwitch.setChecked(false);
        }
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    @Override
    protected void loadFilter() {
        MapFilter mapFilter = MapFilterFactory.getMapFilter();

        entourageNeighborhoodSwitch.setChecked(mapFilter.entourageTypeNeighborhood);
        entouragePrivateCircleSwitch.setChecked(mapFilter.entourageTypePrivateCircle);
        entourageOutingSwitch.setChecked(mapFilter.entourageTypeOuting);
        pastEventsSwitch.setChecked(mapFilter.includePastEvents);
    }

    @Override
    protected void saveFilter() {
        MapFilter mapFilter = MapFilterFactory.getMapFilter();

        mapFilter.entourageTypeNeighborhood = entourageNeighborhoodSwitch.isChecked();
        mapFilter.entourageTypePrivateCircle = entouragePrivateCircleSwitch.isChecked();
        mapFilter.entourageTypeOuting = entourageOutingSwitch.isChecked();
        mapFilter.includePastEvents = pastEventsSwitch.isChecked();
    }
}
