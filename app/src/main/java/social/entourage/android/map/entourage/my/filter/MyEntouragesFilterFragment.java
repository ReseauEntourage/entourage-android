package social.entourage.android.map.entourage.my.filter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
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

    @BindView(R.id.myentourages_filter_unread_switch)
    Switch unreadSwitch;

    @BindView(R.id.myentourages_filter_created_by_me_switch)
    Switch createdByMeOnlySwitch;

    @BindView(R.id.myentourages_filter_partner_switch)
    Switch partnerSwitch;

    @BindView(R.id.myentourages_filter_partner_layout)
    View partnerView;

    @BindView(R.id.myentourages_filter_closed_switch)
    Switch closedSwitch;

    @BindView(R.id.myentourages_filter_demand_switch)
    Switch entourageDemandSwitch;

    @BindView(R.id.myentourages_filter_contribution_switch)
    Switch entourageContributionSwitch;

    @BindView(R.id.myentourages_filter_tours_switch)
    Switch toursSwitch;

    @BindView(R.id.myentourages_filter_tours_layout)
    View toursView;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public MyEntouragesFilterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_my_entourages_filter, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeView();
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    void onBackClicked() {
        EntourageEvents.logEvent(Constants.EVENT_MYENTOURAGES_FILTER_EXIT);
        dismiss();
    }

    @OnClick(R.id.title_action_button)
    void onValidateClicked() {
        // save the values to the filter
        MyEntouragesFilter filter = MyEntouragesFilterFactory.getMyEntouragesFilter(this.getContext());

        filter.setEntourageTypeDemand( entourageDemandSwitch.isChecked() );
        filter.setEntourageTypeContribution( entourageContributionSwitch.isChecked() );
        filter.setShowTours( toursSwitch.isChecked() );

        filter.setShowUnreadOnly( unreadSwitch.isChecked() );
        filter.setShowOwnEntouragesOnly( createdByMeOnlySwitch.isChecked() );
        filter.setShowPartnerEntourages( partnerSwitch.isChecked() );
        filter.setClosedEntourages( closedSwitch.isChecked() );

        MyEntouragesFilterFactory.saveMyEntouragesFilter(filter, this.getContext());

        // inform the app to refrehs the my entourages feed
        BusProvider.getInstance().post(new Events.OnMyEntouragesFilterChanged());

        EntourageEvents.logEvent(Constants.EVENT_MYENTOURAGES_FILTER_SAVE);

        // dismiss the dialog
        dismiss();
    }

    @OnClick(R.id.myentourages_filter_unread_switch)
    protected void onOrganizerSwitch() {
        EntourageEvents.logEvent(Constants.EVENT_MYENTOURAGES_FILTER_UNREAD);
    }

    @OnClick(R.id.myentourages_filter_closed_switch)
    protected void onClosedSwitch() {
        EntourageEvents.logEvent(Constants.EVENT_MYENTOURAGES_FILTER_PAST);
    }

    @OnClick(R.id.myentourages_filter_demand_switch)
    protected void onDemandSwitch() {
        EntourageEvents.logEvent(Constants.EVENT_MYENTOURAGES_FILTER_ASK);
    }

    @OnClick(R.id.myentourages_filter_contribution_switch)
    protected void onContributionSwitch() {
        EntourageEvents.logEvent(Constants.EVENT_MYENTOURAGES_FILTER_OFFER);
    }

    @OnClick(R.id.myentourages_filter_tours_switch)
    protected void onToursSwitch() {
        EntourageEvents.logEvent(Constants.EVENT_MYENTOURAGES_FILTER_TOUR);
    }

    // ----------------------------------
    // Private Methods
    // ----------------------------------

    private void initializeView() {
        MyEntouragesFilter filter = MyEntouragesFilterFactory.getMyEntouragesFilter(this.getContext());

        entourageDemandSwitch.setChecked(filter.isEntourageTypeDemand());
        entourageContributionSwitch.setChecked(filter.isEntourageTypeContribution());
        toursSwitch.setChecked(filter.isShowTours());

        unreadSwitch.setChecked(filter.isShowUnreadOnly());
        createdByMeOnlySwitch.setChecked(filter.isShowOwnEntouragesOnly());
        partnerSwitch.setChecked(filter.isShowPartnerEntourages());
        closedSwitch.setChecked(filter.isClosedEntourages());

        User me = EntourageApplication.me(getActivity());
        boolean isPro = false;
        boolean hasPartner = false;
        if (me != null) {
            isPro = me.isPro();
            hasPartner = me.getPartner() != null;
        }
        // Tours switch is displayed only for pro users
        toursView.setVisibility( isPro ? View.VISIBLE : View.GONE );
        // Partner switch is displayed only if the user has a partner organisation
        partnerView.setVisibility( hasPartner ? View.VISIBLE : View.GONE );
    }

}
