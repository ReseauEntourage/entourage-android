package social.entourage.android.old_v7.entourage.my.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_my_entourages_filter.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.EntourageApplication
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.api.tape.Events.OnMyEntouragesForceRefresh
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.tools.EntBus

/**
 * MyEntourages Filter Fragment
 */
class MyEntouragesFilterFragment  : BaseDialogFragment() {
    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_my_entourages_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        title_close_button?.setOnClickListener {onBackClicked()}
        title_action_button?.setOnClickListener {onValidateClicked()}
        myentourages_filter_unread_switch?.setOnClickListener {onOrganizerSwitch()}
        myentourages_filter_closed_switch?.setOnClickListener {onClosedSwitch()}
        myentourages_filter_demand_switch?.setOnClickListener {onDemandSwitch()}
        myentourages_filter_contribution_switch?.setOnClickListener {onContributionSwitch()}
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------
    fun onBackClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_MYENTOURAGES_FILTER_EXIT)
        dismiss()
    }

    fun onValidateClicked() {
        // save the values to the filter
        val filter = MyEntouragesFilter.get(context)
        myentourages_filter_demand_switch?.let {filter.isEntourageTypeDemand = it.isChecked}
        myentourages_filter_contribution_switch?.let {filter.isEntourageTypeContribution = it.isChecked}
        myentourages_filter_unread_switch?.let {filter.isShowUnreadOnly = it.isChecked}
        //myentourages_filter_created_by_me_switch?.let {filter.showOwnEntouragesOnly = it.isChecked}
        //myentourages_filter_partner_switch?.let {filter.showPartnerEntourages = it.isChecked}
        myentourages_filter_closed_switch?.let {filter.isClosedEntourages = it.isChecked}
        MyEntouragesFilter.save(filter, context)

        // inform the app to refresh the my entourages feed
        EntBus.post(OnMyEntouragesForceRefresh(null))
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_MYENTOURAGES_FILTER_SAVE)

        // dismiss the dialog
        dismiss()
    }

    fun onOrganizerSwitch() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_MYENTOURAGES_FILTER_UNREAD)
    }

    fun onClosedSwitch() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_MYENTOURAGES_FILTER_PAST)
    }

    fun onDemandSwitch() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_MYENTOURAGES_FILTER_ASK)
    }

    fun onContributionSwitch() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_MYENTOURAGES_FILTER_OFFER)
    }

    // ----------------------------------
    // Private Methods
    // ----------------------------------
    private fun initializeView() {
        val filter = MyEntouragesFilter.get(this.context)
        myentourages_filter_demand_switch?.isChecked = filter.isEntourageTypeDemand
        myentourages_filter_contribution_switch?.isChecked = filter.isEntourageTypeContribution
        myentourages_filter_unread_switch?.isChecked = filter.isShowUnreadOnly
        myentourages_filter_created_by_me_switch?.isChecked = filter.showOwnEntouragesOnly
        myentourages_filter_partner_switch?.isChecked = filter.showPartnerEntourages
        myentourages_filter_closed_switch?.isChecked = filter.isClosedEntourages
        val me = EntourageApplication.me(activity)
        // Partner switch is displayed only if the user has a partner organisation
        myentourages_filter_partner_layout?.visibility = if (me?.partner != null) View.VISIBLE else View.GONE
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        const val TAG = "social.entourage_android.MyEntouragesFilterFragment"
    }
}