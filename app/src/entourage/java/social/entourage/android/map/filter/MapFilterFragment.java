package social.entourage.android.map.filter;

import android.graphics.PorterDuff;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.entourage.category.EntourageCategory;
import social.entourage.android.entourage.category.EntourageCategoryManager;

public class MapFilterFragment extends BaseMapFilterFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

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

    @BindView(R.id.map_filter_entourage_outing_switch)
    Switch entourageOutingSwitch;
    @BindView(R.id.map_filter_past_events_switch)
    Switch pastEventsSwitch;

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

    HashMap<String, List<Switch>> actionSwitches = new HashMap<>();
    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener();

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public MapFilterFragment() {
        // Required empty public constructor
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------

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
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_ONLY_MEDICAL_TOURS);
        tourAllSwitch.setChecked(!allToursDisabled());
        tourDetailsLayout.setVisibility(allToursDisabled() ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.map_filter_tour_social_switch)
    protected void onSocialSwitch() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_ONLY_SOCIAL_TOURS);
        tourAllSwitch.setChecked(!allToursDisabled());
        tourDetailsLayout.setVisibility(allToursDisabled() ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.map_filter_tour_distributive_switch)
    protected void onDistributiveSwitch() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_ONLY_DISTRIBUTION_TOURS);
        tourAllSwitch.setChecked(!allToursDisabled());
        tourDetailsLayout.setVisibility(allToursDisabled() ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.map_filter_entourage_outing_switch)
    protected void  onOutingSwitch() {
        if (!entourageOutingSwitch.isChecked()) {
            pastEventsSwitch.setChecked(false);
        }
    }

    @OnClick(R.id.map_filter_entourage_demand_switch)
    protected void onDemandSwitch() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_ONLY_ASK);
        boolean checked = entourageDemandSwitch.isChecked();
        entourageDemandDetailsLayout.setVisibility(checked ? View.VISIBLE : View.GONE);
        List<Switch> switchList = actionSwitches.get(Entourage.TYPE_DEMAND);
        for (Switch categorySwitch: switchList) {
            categorySwitch.setChecked(checked);
        }
    }

    @OnClick(R.id.map_filter_entourage_contribution_switch)
    protected void onContributionSwitch() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_ONLY_OFFERS);
        boolean checked = entourageContributionSwitch.isChecked();
        entourageContributionDetailsLayout.setVisibility(checked ? View.VISIBLE : View.GONE);
        List<Switch> switchList = actionSwitches.get(Entourage.TYPE_CONTRIBUTION);
        for (Switch categorySwitch: switchList) {
            categorySwitch.setChecked(checked);
        }
    }

//    @OnClick(R.id.map_filter_entourage_tours_switch)
//    protected void onOnlyToursSwitch() {
//        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_ONLY_TOURS);
//    }

    @OnClick(R.id.map_filter_entourage_user_only_switch)
    protected void onOnlyMineSwitch() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_ONLY_MINE);
    }

    @OnClick(R.id.map_filter_time_days_1)
    protected void onDays1Click() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_FILTER1);
    }

    @OnClick(R.id.map_filter_time_days_2)
    protected void onDays2Click() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_FILTER2);
    }

    @OnClick(R.id.map_filter_time_days_3)
    protected void onDays3Click() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_FILTER3);
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    @Override
    protected void loadFilter() {

        MapFilter mapFilter = (MapFilter)MapFilterFactory.getMapFilter();

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

        entourageOutingSwitch.setChecked(mapFilter.entourageTypeOuting);
        pastEventsSwitch.setChecked(mapFilter.showPastEvents);

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

    @Override
    protected void saveFilter() {
        MapFilter mapFilter = (MapFilter) MapFilterFactory.getMapFilter();

        mapFilter.tourTypeMedical = tourMedicalSwitch.isChecked();
        mapFilter.tourTypeSocial = tourSocialSwitch.isChecked();
        mapFilter.tourTypeDistributive = tourDistributiveSwitch.isChecked();

        mapFilter.entourageTypeOuting = entourageOutingSwitch.isChecked();
        mapFilter.showPastEvents = pastEventsSwitch.isChecked();

        mapFilter.entourageTypeDemand = entourageDemandSwitch.isChecked();
        mapFilter.entourageTypeContribution = entourageContributionSwitch.isChecked();
        mapFilter.showTours = tourAllSwitch.isChecked();
        mapFilter.onlyMyEntourages = onlyMyEntouragesSwitch.isChecked();
        mapFilter.onlyMyPartnerEntourages = onlyMyPartnerEntouragesSwitch.isChecked();

        for (List<Switch> switchList : actionSwitches.values()) {
            for (Switch categorySwitch : switchList) {
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
        for (EntourageCategory entourageCategory : entourageCategoryList) {
            // inflate and add the view to the layout
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
            EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_ACTION_CATEGORY);
            // get the category
            String category = (String) compoundButton.getTag();
        }
    }

}
