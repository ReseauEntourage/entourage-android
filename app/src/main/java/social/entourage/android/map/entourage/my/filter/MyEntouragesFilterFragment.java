package social.entourage.android.map.entourage.my.filter;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageApplication;
import social.entourage.android.api.model.User;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.R;
import social.entourage.android.tools.BusProvider;

/**
 * MyEntourages Filter Fragment
 */
public class MyEntouragesFilterFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage_android.MyEntouragesFilterFragment";

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @Bind(R.id.myentourages_filter_active_switch)
    Switch activeSwitch;

    @Bind(R.id.myentourages_filter_invited_switch)
    Switch invitedSwitch;

    @Bind(R.id.myentourages_filter_own_switch)
    Switch ownSwitch;

    @Bind(R.id.myentourages_filter_closed_switch)
    Switch closedSwitch;

    @Bind(R.id.myentourages_filter_demand_switch)
    Switch entourageDemandSwitch;

    @Bind(R.id.myentourages_filter_contribution_switch)
    Switch entourageContributionSwitch;

    @Bind(R.id.myentourages_filter_tours_switch)
    Switch toursSwitch;

    @Bind(R.id.myentourages_filter_tours_layout)
    View toursView;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public MyEntouragesFilterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_my_entourages_filter, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeView();
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------

    @OnClick(R.id.myentourages_filter_back_button)
    void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.myentourages_filter_validate_button)
    void onValidateClicked() {
        // save the values to the filter
        MyEntouragesFilter filter = MyEntouragesFilter.getInstance();

        filter.activeEntourages = activeSwitch.isChecked();
        filter.closedEntourages = closedSwitch.isChecked();
        filter.showJoinedEntourages = invitedSwitch.isChecked();
        filter.showOwnEntourages = ownSwitch.isChecked();

        filter.entourageTypeDemand = entourageDemandSwitch.isChecked();
        filter.entourageTypeContribution = entourageContributionSwitch.isChecked();
        filter.showTours = toursSwitch.isChecked();

        // inform the app to refrehs the my entourages feed
        BusProvider.getInstance().post(new Events.OnMyEntouragesFilterChanged());

        // dismiss the dialog
        dismiss();
    }

    // ----------------------------------
    // Private Methods
    // ----------------------------------

    private void initializeView() {
        MyEntouragesFilter filter = MyEntouragesFilter.getInstance();

        activeSwitch.setChecked(filter.activeEntourages);
        invitedSwitch.setChecked(filter.showJoinedEntourages);
        ownSwitch.setChecked(filter.showOwnEntourages);
        closedSwitch.setChecked(filter.closedEntourages);

        entourageDemandSwitch.setChecked(filter.entourageTypeDemand);
        entourageContributionSwitch.setChecked(filter.entourageTypeContribution);
        toursSwitch.setChecked(filter.showTours);

        // Tours switch is displayed only for pro users
        User me = EntourageApplication.me(getActivity());
        boolean isPro = false;
        if (me != null) {
            isPro = me.isPro();
        }
        toursView.setVisibility( isPro ? View.VISIBLE : View.GONE );
    }

}
