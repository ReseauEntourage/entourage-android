package social.entourage.android.map.filter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;

import com.flurry.android.FlurryAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.R;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.tools.BusProvider;

public class MapFilterFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage_android.MapFilterFragment";

    private static final String KEY_PRO_USER = "social,entourage.android.KEY_PRO_USER";

    // ----------------------------------
    // Attributes
    // ----------------------------------
    @BindView(R.id.map_filter_tour_type_layout)
    LinearLayout tourTypeLayout;
    @BindView(R.id.map_filter_entourage_tours)
    View showToursLayout;
    @BindView(R.id.map_filter_tour_medical_switch)
    Switch tourMedicalSwitch;
    @BindView(R.id.map_filter_tour_social_switch)
    Switch tourSocialSwitch;
    @BindView(R.id.map_filter_tour_distributive_switch)
    Switch tourDistributiveSwitch;
    @BindView(R.id.map_filter_entourage_demand_switch)
    Switch entourageDemandSwitch;
    @BindView(R.id.map_filter_entourage_contribution_switch)
    Switch entourageContributionSwitch;
    @BindView(R.id.map_filter_entourage_tours_switch)
    Switch showToursSwitch;
    @BindView(R.id.map_filter_entourage_user_only_switch)
    Switch onlyMyEntouragesSwitch;
    @BindView(R.id.map_filter_time_days_1)
    RadioButton days1RB;
    @BindView(R.id.map_filter_time_days_2)
    RadioButton days2RB;
    @BindView(R.id.map_filter_time_days_3)
    RadioButton days3RB;
    private boolean isProUser = false;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public MapFilterFragment() {
        // Required empty public constructor
    }

    public static MapFilterFragment newInstance(boolean isProUser) {
        MapFilterFragment fragment = new MapFilterFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_PRO_USER, isProUser);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map_filter, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            isProUser = args.getBoolean(KEY_PRO_USER, false);
        }
        initializeView();
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------

    @OnClick(R.id.map_filter_close_button)
    protected void onCloseClicked() {
        FlurryAgent.logEvent(Constants.EVENT_MAP_FILTER_CLOSE);
        dismiss();
    }

    @OnClick(R.id.map_filter_validate_button)
    protected void onValidateClicked() {
        // save the values to the filter
        MapFilter mapFilter = MapFilterFactory.getMapFilter(getContext());

        mapFilter.tourTypeMedical = tourMedicalSwitch.isChecked();
        mapFilter.tourTypeSocial = tourSocialSwitch.isChecked();
        mapFilter.tourTypeDistributive = tourDistributiveSwitch.isChecked();

        mapFilter.entourageTypeDemand = entourageDemandSwitch.isChecked();
        mapFilter.entourageTypeContribution = entourageContributionSwitch.isChecked();
        mapFilter.showTours = showToursSwitch.isChecked();
        mapFilter.onlyMyEntourages = onlyMyEntouragesSwitch.isChecked();

        if (days1RB.isChecked()) {
            mapFilter.timeframe = MapFilter.DAYS_1;
        } else if (days2RB.isChecked()) {
            mapFilter.timeframe = MapFilter.DAYS_2;
        } else if (days3RB.isChecked()) {
            mapFilter.timeframe = MapFilter.DAYS_3;
        }

        // inform the map screen to refresh the newsfeed
        BusProvider.getInstance().post(new Events.OnMapFilterChanged());

        // flurry event
        FlurryAgent.logEvent(Constants.EVENT_MAP_FILTER_SUBMIT);

        // dismiss the dialog
        dismiss();
    }

    @OnClick(R.id.map_filter_tour_medical_switch)
    protected void onMedicalSwitch() {
        FlurryAgent.logEvent(Constants.EVENT_MAP_FILTER_ONLY_MEDICAL_TOURS);
    }

    @OnClick(R.id.map_filter_tour_social_switch)
    protected void onSocialSwitch() {
        FlurryAgent.logEvent(Constants.EVENT_MAP_FILTER_ONLY_SOCIAL_TOURS);
    }

    @OnClick(R.id.map_filter_tour_distributive_switch)
    protected void onDistributiveSwitch() {
        FlurryAgent.logEvent(Constants.EVENT_MAP_FILTER_ONLY_DISTRIBUTION_TOURS);
    }

    @OnClick(R.id.map_filter_entourage_demand_switch)
    protected void onDemandSwitch() {
        FlurryAgent.logEvent(Constants.EVENT_MAP_FILTER_ONLY_ASK);
    }

    @OnClick(R.id.map_filter_entourage_contribution_switch)
    protected void onContributionSwitch() {
        FlurryAgent.logEvent(Constants.EVENT_MAP_FILTER_ONLY_OFFERS);
    }

    @OnClick(R.id.map_filter_entourage_tours_switch)
    protected void onOnlyToursSwitch() {
        FlurryAgent.logEvent(Constants.EVENT_MAP_FILTER_ONLY_TOURS);
    }

    @OnClick(R.id.map_filter_entourage_user_only_switch)
    protected void onOnlyMineSwitch() {
        FlurryAgent.logEvent(Constants.EVENT_MAP_FILTER_ONLY_MINE);
    }

    @OnClick(R.id.map_filter_time_days_1)
    protected void onDays1Click() {
        FlurryAgent.logEvent(Constants.EVENT_MAP_FILTER_FILTER1);
    }

    @OnClick(R.id.map_filter_time_days_2)
    protected void onDays2Click() {
        FlurryAgent.logEvent(Constants.EVENT_MAP_FILTER_FILTER2);
    }

    @OnClick(R.id.map_filter_time_days_3)
    protected void onDays3Click() {
        FlurryAgent.logEvent(Constants.EVENT_MAP_FILTER_FILTER3);
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private void initializeView() {
        tourTypeLayout.setVisibility(isProUser ? View.VISIBLE : View.GONE);
        showToursLayout.setVisibility(isProUser ? View.VISIBLE : View.GONE);

        MapFilter mapFilter = MapFilterFactory.getMapFilter(getContext());

        tourMedicalSwitch.setChecked(mapFilter.tourTypeMedical);
        tourSocialSwitch.setChecked(mapFilter.tourTypeSocial);
        tourDistributiveSwitch.setChecked(mapFilter.tourTypeDistributive);

        entourageDemandSwitch.setChecked(mapFilter.entourageTypeDemand);
        entourageContributionSwitch.setChecked(mapFilter.entourageTypeContribution);
        showToursSwitch.setChecked(mapFilter.showTours);
        onlyMyEntouragesSwitch.setChecked(mapFilter.onlyMyEntourages);

        switch (mapFilter.timeframe) {
            case MapFilter.DAYS_1:
                days1RB.setChecked(true);
                break;
            case MapFilter.DAYS_3:
                days3RB.setChecked(true);
                break;
            case MapFilter.DAYS_2:
            default:
                days2RB.setChecked(true);
                break;
        }
    }

}
