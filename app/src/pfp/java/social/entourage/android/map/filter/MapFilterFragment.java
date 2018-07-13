package social.entourage.android.map.filter;

import android.widget.Switch;

import butterknife.BindView;
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

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public MapFilterFragment() {
        // Required empty public constructor
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------

    // ----------------------------------
    // Private methods
    // ----------------------------------

    @Override
    protected void loadFilter() {
        MapFilter mapFilter = MapFilterFactory.getMapFilter(getContext());

        entourageNeighborhoodSwitch.setChecked(mapFilter.entourageTypeNeighborhood);
        entouragePrivateCircleSwitch.setChecked(mapFilter.entourageTypePrivateCircle);
    }

    @Override
    protected void saveFilter() {
        MapFilter mapFilter = MapFilterFactory.getMapFilter(getContext());

        mapFilter.entourageTypeNeighborhood = entourageNeighborhoodSwitch.isChecked();
        mapFilter.entourageTypePrivateCircle = entouragePrivateCircleSwitch.isChecked();
    }
}
