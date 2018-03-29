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

public class MapFilterFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage_android.MapFilterFragment";

    private static final String KEY_PRO_USER = "social.entourage.android.KEY_PRO_USER";

    // ----------------------------------
    // Attributes
    // ----------------------------------
    @BindView(R.id.map_filter_tour_type_layout)
    LinearLayout tourTypeLayout;
    @BindView(R.id.map_filter_tour_all_switch)
    Switch tourAllSwitch;
    @BindView(R.id.map_filter_tour_type_details_layout)
    View tourDetailsLayout;
    @BindView(R.id.map_filter_tour_medical_switch)
    Switch tourMedicalSwitch;
    @BindView(R.id.map_filter_tour_social_switch)
    Switch tourSocialSwitch;
    @BindView(R.id.map_filter_tour_distributive_switch)
    Switch tourDistributiveSwitch;

    @BindView(R.id.map_filter_entourage_demand_switch)
    Switch entourageDemandSwitch;
    @BindView(R.id.map_filter_entourage_demand_details_layout)
    LinearLayout entourageDemandDetailsLayout;
    @BindView(R.id.map_filter_entourage_contribution_switch)
    Switch entourageContributionSwitch;
    @BindView(R.id.map_filter_entourage_contribution_details_layout)
    LinearLayout entourageContributionDetailsLayout;

    @BindView(R.id.map_filter_entourage_user_only_switch)
    Switch onlyMyEntouragesSwitch;
    @BindView(R.id.map_filter_entourage_partner)
    RelativeLayout onlyMyPartnerEntouragesLayout;
    @BindView(R.id.map_filter_entourage_partner_switch)
    Switch onlyMyPartnerEntouragesSwitch;

    @BindView(R.id.map_filter_time_days_1)
    RadioButton days1RB;
    @BindView(R.id.map_filter_time_days_2)
    RadioButton days2RB;
    @BindView(R.id.map_filter_time_days_3)
    RadioButton days3RB;

    private boolean isProUser = false;

    HashMap<String, List<Switch>> actionSwitches = new HashMap<>();
    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener();

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

    @OnClick(R.id.title_close_button)
    protected void onCloseClicked() {
        EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_CLOSE);
        dismiss();
    }

    @OnClick(R.id.title_action_button)
    protected void onValidateClicked() {
        // save the values to the filter
        MapFilter mapFilter = MapFilterFactory.getMapFilter(getContext());

        mapFilter.tourTypeMedical = tourMedicalSwitch.isChecked();
        mapFilter.tourTypeSocial = tourSocialSwitch.isChecked();
        mapFilter.tourTypeDistributive = tourDistributiveSwitch.isChecked();

        mapFilter.entourageTypeDemand = entourageDemandSwitch.isChecked();
        mapFilter.entourageTypeContribution = entourageContributionSwitch.isChecked();
        mapFilter.showTours = tourAllSwitch.isChecked();
        mapFilter.onlyMyEntourages = onlyMyEntouragesSwitch.isChecked();
        mapFilter.onlyMyPartnerEntourages = onlyMyPartnerEntouragesSwitch.isChecked();

        Iterator<List<Switch>> listIterator =  actionSwitches.values().iterator();
        while (listIterator.hasNext()) {
            List<Switch> switchList = listIterator.next();
            Iterator<Switch> switchIterator = switchList.iterator();
            while (switchIterator.hasNext()) {
                Switch categorySwitch = switchIterator.next();
                if (categorySwitch.getTag() != null) {
                    String category = (String) categorySwitch.getTag();
                    mapFilter.setCategoryChecked(category, categorySwitch.isChecked());
                }
            }
        }

        if (days1RB.isChecked()) {
            mapFilter.timeframe = MapFilter.DAYS_1;
        } else if (days2RB.isChecked()) {
            mapFilter.timeframe = MapFilter.DAYS_2;
        } else if (days3RB.isChecked()) {
            mapFilter.timeframe = MapFilter.DAYS_3;
        }

        // inform the map screen to refresh the newsfeed
        BusProvider.getInstance().post(new Events.OnMapFilterChanged());

        EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_SUBMIT);

        // dismiss the dialog
        dismiss();
    }

    @OnClick(R.id.map_filter_tour_all_switch)
    protected void onAllToursSwitch() {
        boolean checked = tourAllSwitch.isChecked();
        tourMedicalSwitch.setChecked(checked);
        tourSocialSwitch.setChecked(checked);
        tourDistributiveSwitch.setChecked(checked);
        tourDetailsLayout.setVisibility(checked ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.map_filter_tour_medical_switch)
    protected void onMedicalSwitch() {
        EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_ONLY_MEDICAL_TOURS);
        tourAllSwitch.setChecked(!allToursDisabled());
        tourDetailsLayout.setVisibility(allToursDisabled() ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.map_filter_tour_social_switch)
    protected void onSocialSwitch() {
        EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_ONLY_SOCIAL_TOURS);
        tourAllSwitch.setChecked(!allToursDisabled());
        tourDetailsLayout.setVisibility(allToursDisabled() ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.map_filter_tour_distributive_switch)
    protected void onDistributiveSwitch() {
        EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_ONLY_DISTRIBUTION_TOURS);
        tourAllSwitch.setChecked(!allToursDisabled());
        tourDetailsLayout.setVisibility(allToursDisabled() ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.map_filter_entourage_demand_switch)
    protected void onDemandSwitch() {
        EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_ONLY_ASK);
        boolean checked = entourageDemandSwitch.isChecked();
        entourageDemandDetailsLayout.setVisibility(checked ? View.VISIBLE : View.GONE);
        List<Switch> switchList = actionSwitches.get(Entourage.TYPE_DEMAND);
        for (Switch categorySwitch: switchList) {
            categorySwitch.setChecked(checked);
        }
    }

    @OnClick(R.id.map_filter_entourage_contribution_switch)
    protected void onContributionSwitch() {
        EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_ONLY_OFFERS);
        boolean checked = entourageContributionSwitch.isChecked();
        entourageContributionDetailsLayout.setVisibility(checked ? View.VISIBLE : View.GONE);
        List<Switch> switchList = actionSwitches.get(Entourage.TYPE_CONTRIBUTION);
        for (Switch categorySwitch: switchList) {
            categorySwitch.setChecked(checked);
        }
    }

//    @OnClick(R.id.map_filter_entourage_tours_switch)
//    protected void onOnlyToursSwitch() {
//        EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_ONLY_TOURS);
//    }

    @OnClick(R.id.map_filter_entourage_user_only_switch)
    protected void onOnlyMineSwitch() {
        EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_ONLY_MINE);
    }

    @OnClick(R.id.map_filter_time_days_1)
    protected void onDays1Click() {
        EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_FILTER1);
    }

    @OnClick(R.id.map_filter_time_days_2)
    protected void onDays2Click() {
        EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_FILTER2);
    }

    @OnClick(R.id.map_filter_time_days_3)
    protected void onDays3Click() {
        EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_FILTER3);
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private void initializeView() {

        MapFilter mapFilter = MapFilterFactory.getMapFilter(getContext());

        User me = EntourageApplication.me();

        boolean showPartnerFilter = me != null && me.getPartner() != null;
        if (!showPartnerFilter) mapFilter.onlyMyPartnerEntourages = false;

        tourTypeLayout.setVisibility(isProUser ? View.VISIBLE : View.GONE);
        onlyMyPartnerEntouragesLayout.setVisibility(showPartnerFilter ? View.VISIBLE : View.GONE);

        tourMedicalSwitch.setChecked(mapFilter.tourTypeMedical);
        tourSocialSwitch.setChecked(mapFilter.tourTypeSocial);
        tourDistributiveSwitch.setChecked(mapFilter.tourTypeDistributive);
        tourAllSwitch.setChecked(!allToursDisabled());
        tourDetailsLayout.setVisibility(allToursDisabled() ? View.GONE : View.VISIBLE);

        entourageDemandSwitch.setChecked(mapFilter.entourageTypeDemand);
        entourageDemandDetailsLayout.setVisibility(mapFilter.entourageTypeDemand ? View.VISIBLE : View.GONE);
        addEntourageCategories(Entourage.TYPE_DEMAND, entourageDemandDetailsLayout, mapFilter);

        entourageContributionSwitch.setChecked(mapFilter.entourageTypeContribution);
        entourageContributionDetailsLayout.setVisibility(mapFilter.entourageTypeContribution ? View.VISIBLE : View.GONE);
        addEntourageCategories(Entourage.TYPE_CONTRIBUTION, entourageContributionDetailsLayout, mapFilter);

        onlyMyEntouragesSwitch.setChecked(mapFilter.onlyMyEntourages);
        onlyMyPartnerEntouragesSwitch.setChecked(mapFilter.onlyMyPartnerEntourages);

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

    private boolean allToursDisabled() {
        return !tourMedicalSwitch.isChecked() & !tourSocialSwitch.isChecked() & !tourDistributiveSwitch.isChecked();
    }

    private void addEntourageCategories(String entourageType, LinearLayout layout, MapFilter mapFilter) {
        // create the hashmap entrance
        List<Switch> switchList = new ArrayList<>();
        actionSwitches.put(entourageType, switchList);
        // get the list of categories
        EntourageCategoryManager categoryManager = EntourageCategoryManager.getInstance();
        List<EntourageCategory> entourageCategoryList = categoryManager.getEntourageCategoriesForType(entourageType);
        if (entourageCategoryList == null) return;
        Iterator<EntourageCategory> iterator = entourageCategoryList.iterator();
        while (iterator.hasNext()) {
            // inflate and add the view to the layout
            EntourageCategory entourageCategory = iterator.next();
            View view = getLayoutInflater().inflate(R.layout.layout_filter_item_map, layout, false);
            view.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.addView(view);

            // populate the view
            TextView mFilterName = view.findViewById(R.id.filter_item_text);
            ImageView mFilterImage = view.findViewById(R.id.filter_item_image);
            Switch mFilterSwitch = view.findViewById(R.id.filter_item_switch);
            View mSeparatorView = view.findViewById(R.id.filter_item_separator);

            mFilterName.setText(entourageCategory.getTitle());
            mFilterImage.setImageResource(entourageCategory.getIconRes());
            mFilterImage.clearColorFilter();
            if (getContext() != null) {
                mFilterImage.setColorFilter(ContextCompat.getColor(getContext(), entourageCategory.getTypeColorRes()), PorterDuff.Mode.SRC_IN);
            }
            mFilterSwitch.setChecked(mapFilter.isCategoryChecked(entourageCategory));
            mFilterSwitch.setTag(entourageCategory.getKey());
            mFilterSwitch.setOnCheckedChangeListener(onCheckedChangeListener);

            switchList.add(mFilterSwitch);
        }
    }

    private class OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
            // if no tag, exit
            if (compoundButton.getTag() == null) {
                return;
            }
            EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_ACTION_CATEGORY);
            // get the category
            String category = (String) compoundButton.getTag();
        }
    }

}
