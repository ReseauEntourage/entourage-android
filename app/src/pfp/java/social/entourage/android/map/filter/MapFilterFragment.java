package social.entourage.android.map.filter;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.map.entourage.category.EntourageCategory;
import social.entourage.android.map.entourage.category.EntourageCategoryManager;
import social.entourage.android.tools.BusProvider;

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
        MapFilterPFP mapFilter = (MapFilterPFP) MapFilterFactory.getMapFilter(getContext());

        entourageNeighborhoodSwitch.setChecked(mapFilter.entourageTypeNeighborhood);
        entouragePrivateCircleSwitch.setChecked(mapFilter.entourageTypePrivateCircle);
    }

    @Override
    protected void saveFilter() {
        MapFilterPFP mapFilter = (MapFilterPFP) MapFilterFactory.getMapFilter(getContext());

        mapFilter.entourageTypeNeighborhood = entourageNeighborhoodSwitch.isChecked();
        mapFilter.entourageTypePrivateCircle = entouragePrivateCircleSwitch.isChecked();
    }
}
